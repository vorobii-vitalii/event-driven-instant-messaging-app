package org.instant.messaging.app.kafka;

import java.util.Collection;

import org.instant.messaging.app.actor.dialog.DialogActor;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command_handler.DialogCommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.event_handler.DialogEventHandlerConfigurer;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.external.ExternalShardAllocationStrategy;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.kafka.cluster.sharding.KafkaClusterSharding;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DialogEventsProcessorInitializer {
	private final Collection<DialogCommandHandlerConfigurer> dialogCommandHandlerConfigurers;
	private final Collection<DialogEventHandlerConfigurer> dialogEventHandlerConfigurers;

	public void initialize(
			DialogEventsProcessorConfig eventsProcessorConfig,
			ActorSystem<?> system
	) {
		log.info("Going to initialize Kafka sharded dialog events processor. Configuration = {}", eventsProcessorConfig);
		KafkaClusterSharding.get(system)
				.messageExtractorNoEnvelope(
						eventsProcessorConfig.topic(),
						eventsProcessorConfig.partitionsFetchTimeout(),
						DialogCommand::dialogId,
						eventsProcessorConfig.getConsumerSettings()
				).thenApply(messageExtractor -> {
					log.info("Kafka message extractor created. Starting sharded process for processing dialog events...");
					var entity = Entity.of(
									DialogActor.ENTITY_KEY,
									entityContext -> Behaviors.setup(actorContext -> {
										var behavior = new DialogActor(
												PersistenceId.of(DialogActor.ENTITY_KEY.name(), entityContext.getEntityId()),
												eventsProcessorConfig.performSnapshotsAfterEvents(),
												actorContext,
												dialogCommandHandlerConfigurers,
												dialogEventHandlerConfigurers
										);
										return EventSourcedBehavior.start(behavior, actorContext);
									}))
							.withAllocationStrategy(ExternalShardAllocationStrategy.create(system, DialogActor.ENTITY_KEY.name()))
							.withMessageExtractor(messageExtractor);
					return ClusterSharding.get(system).init(entity);
				});
	}

}
