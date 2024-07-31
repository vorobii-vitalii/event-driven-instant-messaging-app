package org.instant.messaging.app.actor.dialog.event_handler;

import java.util.Set;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.Message;

import akka.persistence.typed.javadsl.EventHandlerBuilder;

public class MessageSentEventHandlerConfigurer implements DialogEventHandlerConfigurer {

	@Override
	public void configure(EventHandlerBuilder<DialogState, DialogEvent> eventHandlerBuilder) {
		eventHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onEvent(MessageSentEvent.class, (state, event) -> state.addMessage(new Message(
						event.messageId(),
						event.from(),
						event.messageContent(),
						event.timestamp(),
						Set.of()
				)));
	}

}
