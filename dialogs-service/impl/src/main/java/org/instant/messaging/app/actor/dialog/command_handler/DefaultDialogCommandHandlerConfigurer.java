package org.instant.messaging.app.actor.dialog.command_handler;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.actor.typed.javadsl.ActorContext;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EffectFactories;

public class DefaultDialogCommandHandlerConfigurer implements DialogCommandHandlerConfigurer {

	@Override
	public void configure(
			CommandHandlerWithReplyBuilder<DialogCommand, DialogEvent, DialogState> commandHandlerBuilder,
			ActorContext<DialogCommand> actorContext
	) {
		var log = actorContext.getLog();
		commandHandlerBuilder
				.forAnyState()
				.onAnyCommand((dialogState, command) -> {
					log.warn("In state = {} command = {} is not applicable", dialogState, command);
					return new EffectFactories<DialogEvent, DialogState>()
							.none()
							.thenReply(command.replyTo(), v -> StatusReply.error("Command not applicable in this state"));
				});
	}

	@Override
	public int getPriority() {
		return 1;
	}

}
