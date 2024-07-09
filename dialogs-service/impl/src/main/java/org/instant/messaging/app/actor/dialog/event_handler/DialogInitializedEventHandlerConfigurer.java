package org.instant.messaging.app.actor.dialog.event_handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.DialogInitializedEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.NotInitializedDialog;

import akka.persistence.typed.javadsl.EventHandlerBuilder;

public class DialogInitializedEventHandlerConfigurer implements DialogEventHandlerConfigurer {

	@Override
	public void configure(EventHandlerBuilder<DialogState, DialogEvent> eventHandlerBuilder) {
		eventHandlerBuilder
				.forStateType(NotInitializedDialog.class)
				.onEvent(DialogInitializedEvent.class, (ignoredState, dialogInitialized) -> {
					Set<UUID> allParticipants = new HashSet<>(dialogInitialized.invitedParticipants());
					allParticipants.add(dialogInitialized.createdBy());
					return new ActiveDialogState(
							dialogInitialized.dialogTopic(),
							new HashMap<>(),
							dialogInitialized.createdBy(),
							dialogInitialized.createdBy(),
							allParticipants
					);
				});
	}

}
