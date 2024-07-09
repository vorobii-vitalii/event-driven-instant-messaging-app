package org.instant.messaging.app.actor.dialog.event_handler;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.junit.jupiter.api.Test;

import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;

abstract class BaseDialogEventHandlerConfigurerTest {

	protected EventHandler<DialogState, DialogEvent> eventHandler() {
		EventHandlerBuilder<DialogState, DialogEvent> eventHandlerBuilder = new EventHandlerBuilder<>();
		configurer().configure(eventHandlerBuilder);
		return eventHandlerBuilder.build();
	}

	protected abstract DialogEventHandlerConfigurer configurer();

}
