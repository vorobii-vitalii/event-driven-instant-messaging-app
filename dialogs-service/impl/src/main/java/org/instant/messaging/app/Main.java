package org.instant.messaging.app;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.instant.message.app.DialogReadService;
import org.instant.message.app.DialogReadServiceHandlerFactory;
import org.instant.message.app.DialogWriteService;
import org.instant.message.app.DialogWriteServiceHandlerFactory;
import org.instant.messaging.app.dependency_injection.components.DaggerDialogEventsProcessorComponent;
import org.instant.messaging.app.grpc.config.DialogGrpcServiceConfigReader;
import org.instant.messaging.app.grpc.services.DialogReadServiceImpl;
import org.instant.messaging.app.grpc.services.DialogWriteServiceImpl;
import org.instant.messaging.app.kafka.DialogEventsProcessorConfigReader;
import org.instant.messaging.app.kafka.KafkaProducerSettingsReader;

import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.grpc.javadsl.ServerReflection;
import akka.grpc.javadsl.ServiceHandler;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.function.Function;
import akka.kafka.javadsl.SendProducer;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.scaladsl.AkkaManagement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
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

					});

			return Behaviors.ignore();
		});
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
