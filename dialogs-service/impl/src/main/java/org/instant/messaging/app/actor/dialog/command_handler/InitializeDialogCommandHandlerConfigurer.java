package org.instant.messaging.app.actor.dialog.command_handler;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.InitializeDialogCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.DialogInitializedEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.NotInitializedDialog;

import akka.actor.typed.javadsl.ActorContext;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EffectFactories;

public class InitializeDialogCommandHandlerConfigurer implements DialogCommandHandlerConfigurer {

	@Override
	public void configure(
			CommandHandlerWithReplyBuilder<DialogCommand, DialogEvent, DialogState> commandHandlerBuilder,
			ActorContext<DialogCommand> actorContext
	) {
		var log = actorContext.getLog();
		commandHandlerBuilder
				.forStateType(NotInitializedDialog.class)
				.onCommand(InitializeDialogCommand.class, (state, command) -> {
					log.info("Initializing dialog. Command = {}", command);
					return new EffectFactories<DialogEvent, DialogState>()
							.persist(new DialogInitializedEvent(
									command.requester(),
									command.otherParticipants(),
									command.dialogTopic(),
									command.initializedAt()
							))
							.thenReply(command.replyTo(), ignored -> StatusReply.Ack());
				});
	}
}
