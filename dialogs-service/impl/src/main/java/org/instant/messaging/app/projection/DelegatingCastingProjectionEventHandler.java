package org.instant.messaging.app.projection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelegatingCastingProjectionEventHandler<E> implements ProjectionEventHandler<E> {
	private final Map<Class<? extends E>, ProjectionEventHandler<E>> eventHandlerMap;

	public DelegatingCastingProjectionEventHandler(Collection<CastingProjectionEventHandler<E, ? extends E>> eventHandlers) {
		eventHandlerMap = eventHandlers.stream()
				.collect(Collectors.toMap(CastingProjectionEventHandler::subType, v -> v));
	}

	@Override
	public CompletionStage<Done> handleEvent(E event, String entityId, R2dbcSession session) {
		ProjectionEventHandler<E> eventHandler = eventHandlerMap.get(event.getClass());
		if (eventHandler == null) {
			log.warn("Ignoring event = {} for entity = {}", event, entityId);
			return CompletableFuture.completedFuture(Done.done());
		}
		log.info("Handling event {} for {} using {}", event, entityId, eventHandler.getClass());
		return eventHandler.handleEvent(event, entityId, session);
	}
}
