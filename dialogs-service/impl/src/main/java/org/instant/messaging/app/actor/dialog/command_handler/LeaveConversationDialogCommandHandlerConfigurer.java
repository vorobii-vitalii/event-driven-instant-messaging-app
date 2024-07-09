package org.instant.messaging.app.actor.dialog.command_handler;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.LeaveConversationCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.ParticipantLeftEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.actor.typed.javadsl.ActorContext;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EffectFactories;

public class LeaveConversationDialogCommandHandlerConfigurer implements DialogCommandHandlerConfigurer {

	@Override
	public void configure(
			CommandHandlerWithReplyBuilder<DialogCommand, DialogEvent, DialogState> commandHandlerBuilder,
			ActorContext<DialogCommand> actorContext
	) {
		var log = actorContext.getLog();
		commandHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onCommand(LeaveConversationCommand.class, (state, command) -> {
					var requester = command.requester();
					if (state.isParticipantAbsent(requester)) {
						log.info("Requester {} is not part of dialog already", requester);
						return new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(command.replyTo(), v -> StatusReply.ack());
					}
					log.info("Participant {} leaving conversation", requester);
					return new EffectFactories<DialogEvent, DialogState>()
							.persist(new ParticipantLeftEvent(requester, command.timestamp()))
							.thenReply(command.replyTo(), v -> StatusReply.ack());
				});
	}

}
