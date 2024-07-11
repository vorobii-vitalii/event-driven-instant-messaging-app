package org.instant.messaging.app.actor.dialog.event_handler;

import org.instant.messaging.app.actor.dialog.event.DialogClosedEvent;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.ClosedDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.persistence.typed.javadsl.EventHandlerBuilder;

// TODO: Register
public class DialogClosedEventHandlerConfigurer implements DialogEventHandlerConfigurer {

	@Override
	public void configure(EventHandlerBuilder<DialogState, DialogEvent> eventHandlerBuilder) {
		eventHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onEvent(DialogClosedEvent.class, (state, event) -> new ClosedDialogState(event.timestamp()));
	}

}
