package org.instant.messaging.app.r2dbc.impl;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.instant.messaging.app.r2dbc.R2dbcSessionExecutor;

import akka.actor.typed.ActorSystem;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class R2dbcSessionExecutorImpl implements R2dbcSessionExecutor {
	private final ActorSystem<?> actorSystem;

	public R2dbcSessionExecutorImpl(ActorSystem<?> actorSystem) {
		this.actorSystem = actorSystem;
	}

	@Override
	public <T> CompletionStage<T> execute(Function<R2dbcSession, CompletionStage<T>> action) {
		return R2dbcSession.withSession(actorSystem, action);
	}
}
