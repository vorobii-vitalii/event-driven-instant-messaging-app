package org.instant.messaging.app.actor.command;

import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;

public interface CommandHandlerConfigurer<C, E, S> {
	void configure(CommandHandlerWithReplyBuilder<C, E, S> commandHandlerBuilder, ActorContext<C> actorContext);
}
