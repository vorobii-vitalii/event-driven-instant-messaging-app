package org.instant.messaging.app.actor.dialog.event_handler;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.ParticipantLeftEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.ClosedDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.persistence.typed.javadsl.EventHandlerBuilder;

public class ParticipantLeftEventHandlerConfigurer implements DialogEventHandlerConfigurer {

	@Override
	public void configure(EventHandlerBuilder<DialogState, DialogEvent> eventHandlerBuilder) {
		eventHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onEvent(ParticipantLeftEvent.class, (state, event) -> {
					var requester = event.requester();
					var dialogStateFollowingRemoval = state.removeParticipant(requester);
					if (dialogStateFollowingRemoval.isEmptyConversation()) {
						return new ClosedDialogState(event.timestamp());
					}
					if (dialogStateFollowingRemoval.isLeader(requester)) {
						var newLeader = dialogStateFollowingRemoval.participants()
								.stream()
								.findFirst()
								.orElseThrow();
						return dialogStateFollowingRemoval.updateLeader(newLeader);
					}
					return dialogStateFollowingRemoval;
				});
	}

}
