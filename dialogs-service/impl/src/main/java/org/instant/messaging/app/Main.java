package org.instant.messaging.app;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.message.app.DialogReadService;
import org.instant.message.app.DialogReadServiceHandlerFactory;
import org.instant.message.app.DialogWriteService;
import org.instant.message.app.DialogWriteServiceHandlerFactory;
import org.instant.messaging.app.actor.dialog.DialogActor;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.dependency_injection.components.DaggerDialogEventsProcessorComponent;
import org.instant.messaging.app.dependency_injection.components.DaggerDialogEventsProjectionComponent;
import org.instant.messaging.app.grpc.config.DialogGrpcServiceConfigReader;
import org.instant.messaging.app.grpc.services.DialogReadServiceImpl;
import org.instant.messaging.app.grpc.services.DialogWriteServiceImpl;
import org.instant.messaging.app.kafka.DialogEventsProcessorConfigReader;
import org.instant.messaging.app.kafka.KafkaProducerSettingsReader;
import org.instant.messaging.app.projection.DelegatingProjectionHandler;

import com.typesafe.config.ConfigFactory;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.grpc.javadsl.ServerReflection;
import akka.grpc.javadsl.ServiceHandler;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.function.Function;
import akka.kafka.CommitterSettings;
import akka.kafka.Subscriptions;
import akka.kafka.cluster.sharding.KafkaClusterSharding;
import akka.kafka.javadsl.Committer;
import akka.kafka.javadsl.Consumer;
import akka.kafka.javadsl.SendProducer;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.scaladsl.AkkaManagement;
import akka.pattern.StatusReply;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	public static final Duration PROCESSING_TIMEOUT = Duration.ofSeconds(5);
	private static final String ACTOR_SYSTEM_NAME = "dialogs-service";

	public static void start(String bindAddress) {
		log.info("Start on address = {}", bindAddress);
		var config = ConfigFactory.parseString("""
				  akka.management.http.hostname = "%s"
				  akka.remote.artery.canonical.hostname = "%s"
				  dialog-grpc-service.interface = "%s"
				""".formatted(bindAddress, bindAddress, bindAddress)).withFallback(ConfigFactory.load());
		ActorSystem.create(guardianBehavior(), ACTOR_SYSTEM_NAME, config);
	}

	private static Behavior<Object> guardianBehavior() {
		return Behaviors.setup(context -> {
			var system = context.getSystem();
			AkkaManagement.get(system).start();
			ClusterBootstrap.get(system).start();

			startGrpcServer(system);
			startProjection(system);
			startEventsProcessor(system);

			return Behaviors.ignore();
		});
	}

	private static void startEventsProcessor(ActorSystem<Void> system) {
		var config = system.settings().config();
		var dialogEventsProcessorConfig = new DialogEventsProcessorConfigReader().readConfig(config, system);
		var eventsProcessorComponent = DaggerDialogEventsProcessorComponent.create();
		var messageAdapter = eventsProcessorComponent.dialogKafkaMessageAdapter();

		eventsProcessorComponent
				.dialogEventsProcessorInitializer()
				.initialize(dialogEventsProcessorConfig, system)
				.thenAccept(dialogCommandActorRef -> {
					var rebalanceListener = KafkaClusterSharding.get(system).rebalanceListener(DialogActor.ENTITY_KEY);
					var subscription = Subscriptions.topics(dialogEventsProcessorConfig.topic())
							.withRebalanceListener(Adapter.toClassic(rebalanceListener));
					var consumerSettings = dialogEventsProcessorConfig.getConsumerSettings();
					CommitterSettings committerSettings = CommitterSettings.create(config.getConfig("akka.kafka.committer"));
					Consumer.committableSource(consumerSettings, subscription)
							.mapAsync(1, committableMessage -> {
								var record = committableMessage.record();
								log.info("Consuming record = {}", record);
								var dialogKafkaMessage = DialogKafkaMessage.parseFrom(record.value());
								log.info("Dialog kafka message = {}", dialogKafkaMessage);
								if (!messageAdapter.isSupported(dialogKafkaMessage)) {
									log.warn("Message {} cannot converted to command...", dialogKafkaMessage);
									return CompletableFuture.completedFuture(committableMessage.committableOffset());
								}
								log.info("Message is supported!");
								try {
									CompletionStage<StatusReply<Done>> askReply =
											AskPattern.ask(
													dialogCommandActorRef,
													ref -> {
														var command = messageAdapter.adaptMessage(dialogKafkaMessage, ref).orElseThrow();
														log.info("Converter to command = {}", command);
														return command;
													},
													PROCESSING_TIMEOUT,
													system.scheduler()
											);
									return askReply
											.handle((v, err) -> {
												if (err != null) {
													log.error("Error on message processing!", err);
												} else {
													log.info("Processing res = {}. Committing offset", v);
												}
												return committableMessage.committableOffset();
											});
								}
								catch (Exception error) {
									log.error("Error on processing", error);
									return CompletableFuture.completedFuture(committableMessage.committableOffset());
								}
							})
							.toMat(Committer.sink(committerSettings.withMaxBatch(1)), Consumer::createDrainingControl)
							.run(system);
				});
	}

	private static void startProjection(ActorSystem<Void> system) {
		DelegatingProjectionHandler.initProjectionProcess(
				system,
				DaggerDialogEventsProjectionComponent.create().dialogEventsProjectionHandler(),
				4,
				"DialogEventsDatabaseUpdateProjection",
				DialogActor.ENTITY_KEY.name(),
				"dialog-events-"
		);
	}

	private static void startGrpcServer(ActorSystem<?> system) {
		var config = system.settings().config();

		var dialogGrpcServiceConfig = new DialogGrpcServiceConfigReader().readConfig(config);

		var producerSettings = new KafkaProducerSettingsReader().readProducerSettings(config);

		var producer = new SendProducer<>(producerSettings, system);

		var dialogWriteService = new DialogWriteServiceImpl(producer, "dialog-commands");
		var dialogReadService = new DialogReadServiceImpl(system, DaggerDialogEventsProjectionComponent.create().dialogRepository());

		@SuppressWarnings("unchecked")
		Function<HttpRequest, CompletionStage<HttpResponse>> serviceHandlers = ServiceHandler.concatOrNotFound(
				DialogWriteServiceHandlerFactory.create(dialogWriteService, system),
				DialogReadServiceHandlerFactory.create(dialogReadService, system),
				ServerReflection.create(List.of(DialogWriteService.description, DialogReadService.description), system)
		);

		log.info("Starting HTTP/gRPC server on port {}", dialogGrpcServiceConfig.port());

		Http.get(system)
				.newServerAt(dialogGrpcServiceConfig.networkInterface(), dialogGrpcServiceConfig.port())
				.bind(serviceHandlers)
				.whenComplete((binding, failure) -> {
					if (failure == null) {
						system.log().info("HTTP server now listening at port {}", dialogGrpcServiceConfig.port());
					} else {
						system.log().error("Failed to bind HTTP server, terminating.", failure);
						system.terminate();
					}
				});
	}

}
