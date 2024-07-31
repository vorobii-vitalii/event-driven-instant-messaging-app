package org.instant.messaging.app.r2dbc;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface R2dbcSessionExecutor {
	<T> CompletionStage<T> execute(Function<R2dbcSession, CompletionStage<T>> action);
}
