package org.instant.messaging.app.actor.dialog.event_handler;

import java.time.Instant;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;

abstract class BaseDialogEventHandlerConfigurerTest {
	protected static final Instant TIMESTAMP = Instant.now();

	protected EventHandler<DialogState, DialogEvent> eventHandler() {
		EventHandlerBuilder<DialogState, DialogEvent> eventHandlerBuilder = new EventHandlerBuilder<>();
		configurer().configure(eventHandlerBuilder);
		return eventHandlerBuilder.build();
	}

	protected abstract DialogEventHandlerConfigurer configurer();

}
