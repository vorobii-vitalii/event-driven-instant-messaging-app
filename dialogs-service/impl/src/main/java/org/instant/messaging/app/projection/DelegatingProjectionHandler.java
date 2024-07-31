package org.instant.messaging.app.projection;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.instant.messaging.app.projection.impl.EntityIdExtractorImpl;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.cluster.sharding.typed.ShardedDaemonProcessContext;
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings;
import akka.cluster.sharding.typed.javadsl.ShardedDaemonProcess;
import akka.japi.Pair;
import akka.persistence.query.Offset;
import akka.persistence.query.typed.EventEnvelope;
import akka.persistence.r2dbc.query.javadsl.R2dbcReadJournal;
import akka.projection.Projection;
import akka.projection.ProjectionBehavior;
import akka.projection.ProjectionId;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.SourceProvider;
import akka.projection.r2dbc.R2dbcProjectionSettings;
import akka.projection.r2dbc.javadsl.R2dbcHandler;
import akka.projection.r2dbc.javadsl.R2dbcProjection;
import akka.projection.r2dbc.javadsl.R2dbcSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelegatingProjectionHandler<EventType> extends R2dbcHandler<EventEnvelope<EventType>> {
	private final ProjectionEventHandler<EventType> projectionEventHandler;
	private final EntityIdExtractor entityIdExtractor;

	public DelegatingProjectionHandler(ProjectionEventHandler<EventType> projectionEventHandler, EntityIdExtractor entityIdExtractor) {
		this.projectionEventHandler = projectionEventHandler;
		this.entityIdExtractor = entityIdExtractor;
	}

	public static <T> void initProjectionProcess(
			ActorSystem<?> system,
			ProjectionEventHandler<T> projectionEventHandler,
			int numInstances,
			String projectionName,
			String entityName,
			String sliceSuffix
	) {
		ShardedDaemonProcess.get(system)
				.initWithContext(
						ProjectionBehavior.Command.class,
						projectionName,
						numInstances,
						daemonContext ->
								createProjectionBehavior(system, daemonContext, projectionEventHandler, projectionName, entityName, sliceSuffix),
						ShardedDaemonProcessSettings.create(system),
						Optional.of(ProjectionBehavior.stopMessage()));
	}

	private static <T> Behavior<ProjectionBehavior.Command> createProjectionBehavior(
			ActorSystem<?> system,
			ShardedDaemonProcessContext daemonContext,
			ProjectionEventHandler<T> projectionEventHandler,
			String projectionName,
			String entityName,
			String sliceSuffix
	) {
		var sliceRanges = EventSourcedProvider.sliceRanges(system, R2dbcReadJournal.Identifier(), daemonContext.totalProcesses());
		var sliceRange = sliceRanges.get(daemonContext.processNumber());
		return ProjectionBehavior.create(createProjection(system, sliceRange, projectionEventHandler, projectionName, entityName, sliceSuffix));
	}

	private static <T> Projection<EventEnvelope<T>> createProjection(
			ActorSystem<?> system,
			Pair<Integer, Integer> sliceRange,
			ProjectionEventHandler<T> projectionEventHandler,
			String projectionName,
			String entityName,
			String sliceSuffix
	) {
		int minSlice = sliceRange.first();
		int maxSlice = sliceRange.second();
		SourceProvider<Offset, EventEnvelope<T>> sourceProvider =
				EventSourcedProvider.eventsBySlices(system, R2dbcReadJournal.Identifier(), entityName, minSlice, maxSlice);
		String slice = sliceSuffix + minSlice + '-' + maxSlice;
		log.info("Creating projection {} for slice {}", projectionName, slice);
		Optional<R2dbcProjectionSettings> settings = Optional.empty();
		ProjectionId projectionId = ProjectionId.of(projectionName, slice);
		Supplier<R2dbcHandler<EventEnvelope<T>>> handlerSupplier =
				() -> new DelegatingProjectionHandler<>(projectionEventHandler, new EntityIdExtractorImpl());
		return R2dbcProjection.exactlyOnce(projectionId, settings, sourceProvider, handlerSupplier, system);
	}

	@Override
	public CompletionStage<Done> process(R2dbcSession session, EventEnvelope<EventType> eventEnvelope) {
		log.info("Processing event {}", eventEnvelope);
		EventType event = eventEnvelope.getEvent();
		return projectionEventHandler.handleEvent(event, entityIdExtractor.extractEntityId(eventEnvelope.persistenceId()), session);
	}

}
