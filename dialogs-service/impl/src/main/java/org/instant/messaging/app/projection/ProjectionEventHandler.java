package org.instant.messaging.app.projection;

import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface ProjectionEventHandler<E> {
	CompletionStage<Done> handleEvent(E event, String entityId, R2dbcSession session);
}
