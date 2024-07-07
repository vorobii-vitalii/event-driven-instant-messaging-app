package org.instant.messaging.app.actor.event;

import akka.persistence.typed.javadsl.EventHandlerBuilder;

public interface EventHandlerConfigurer<S, E> {
	void configure(EventHandlerBuilder<S, E> eventHandlerBuilder);
}
