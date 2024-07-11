package org.instant.messaging.app.projection;

import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface CastingProjectionEventHandler<E, S extends E> extends ProjectionEventHandler<E> {

	@SuppressWarnings("unchecked")
	default CompletionStage<Done> handleEvent(E event, String entityId, R2dbcSession session) {
		return handleSubTypeEvent((S) event, entityId, session);
	}

	Class<? extends E> subType();

	CompletionStage<Done> handleSubTypeEvent(S subType, String entityId, R2dbcSession session);

}
