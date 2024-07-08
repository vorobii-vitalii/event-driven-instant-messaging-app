package org.instant.messaging.app;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.message.app.DialogReadService;
import org.instant.message.app.DialogReadServiceHandlerFactory;
import org.instant.message.app.DialogWriteService;
import org.instant.message.app.DialogWriteServiceHandlerFactory;
import org.instant.messaging.app.actor.dialog.DialogActor;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.InitializeDialogCommand;
import org.instant.messaging.app.dependency_injection.components.DaggerDialogEventsProcessorComponent;
import org.instant.messaging.app.grpc.config.DialogGrpcServiceConfigReader;
import org.instant.messaging.app.grpc.services.DialogReadServiceImpl;
import org.instant.messaging.app.grpc.services.DialogWriteServiceImpl;
import org.instant.messaging.app.kafka.DialogEventsProcessorConfigReader;
import org.instant.messaging.app.kafka.KafkaProducerSettingsReader;

import com.typesafe.config.ConfigFactory;

import akka.Done;
import akka.actor.typed.ActorRef;
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

			var config = system.settings().config();

			var dialogEventsProcessorConfig = new DialogEventsProcessorConfigReader().readConfig(config, system);

			DaggerDialogEventsProcessorComponent.create()
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
									return AskPattern.ask(
													dialogCommandActorRef,
													(ActorRef<StatusReply<Done>> ref) -> toCommand(dialogKafkaMessage, ref),
													PROCESSING_TIMEOUT,
													system.scheduler())
											.thenApply(v -> {
												log.info("Processing res = {}. Committing offset", v);
												return committableMessage.committableOffset();
											});
								})
								.toMat(Committer.sink(committerSettings.withMaxBatch(1)), Consumer::createDrainingControl)
								.run(system);
					});

			return Behaviors.ignore();
		});
	}

	private static DialogCommand toCommand(DialogKafkaMessage dialogKafkaMessage, ActorRef<StatusReply<Done>> replyAcceptor) {
		switch (dialogKafkaMessage.getMessageCase()) {
			case INIT_DIALOG -> {
				var initDialog = dialogKafkaMessage.getInitDialog();
				return InitializeDialogCommand.builder()
						.replyTo(replyAcceptor)
						.initializedAt(Instant.ofEpochMilli(initDialog.getTimestamp()))
						.dialogTopic(initDialog.getDialogTopic())
						.otherParticipants(
								initDialog.getParticipantsToInviteList()
										.stream()
										.map(v -> UUID.fromString(v.getValue()))
										.toList())
						.requester(UUID.fromString(initDialog.getRequester().getValue()))
						.dialogId(initDialog.getDialogId().getValue())
						.build();
			}
			default -> throw new IllegalStateException();
		}
	}

	private static void startGrpcServer(ActorSystem<?> system) {
		var config = system.settings().config();

		var dialogGrpcServiceConfig = new DialogGrpcServiceConfigReader().readConfig(config);

		var producerSettings = new KafkaProducerSettingsReader().readProducerSettings(config);

		var producer = new SendProducer<>(producerSettings, system);

		var dialogWriteService = new DialogWriteServiceImpl(producer, "dialog-commands");
		var dialogReadService = new DialogReadServiceImpl();

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
