package org.instant.messaging.app.actor.dialog.event_handler;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageRemovedEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.persistence.typed.javadsl.EventHandlerBuilder;

public class MessageRemovedEventHandlerConfigurer implements DialogEventHandlerConfigurer {

	@Override
	public void configure(EventHandlerBuilder<DialogState, DialogEvent> eventHandlerBuilder) {
		eventHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onEvent(MessageRemovedEvent.class, (state, event) -> state.removeMessage(event.messageId()));
	}

}
