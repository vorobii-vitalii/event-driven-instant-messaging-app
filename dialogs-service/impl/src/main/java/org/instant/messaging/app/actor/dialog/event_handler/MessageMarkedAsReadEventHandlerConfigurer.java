package org.instant.messaging.app.actor.dialog.event_handler;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageMarkedAsReadEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.persistence.typed.javadsl.EventHandlerBuilder;

public class MessageMarkedAsReadEventHandlerConfigurer implements DialogEventHandlerConfigurer {

	@Override
	public void configure(EventHandlerBuilder<DialogState, DialogEvent> eventHandlerBuilder) {
		eventHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onEvent(MessageMarkedAsReadEvent.class, (state, messageMarkedAsRead) -> state.changeMessageById(
						messageMarkedAsRead.messageId(),
						message -> message.markAsReadBy(messageMarkedAsRead.requester())
				));
	}

}
