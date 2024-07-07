package org.instant.messaging.app.actor.command;

import java.util.function.Supplier;

import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EffectFactories;

public interface CommandHandlerConfigurer<C, E, S> {
	void configure(
			CommandHandlerWithReplyBuilder<C, E, S> commandHandlerBuilder,
			ActorContext<C> actorContext,
			Supplier<EffectFactories<E, S>> effectFactory
	);
}
