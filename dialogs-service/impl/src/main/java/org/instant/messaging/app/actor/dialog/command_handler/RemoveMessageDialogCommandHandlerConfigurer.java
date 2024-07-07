package org.instant.messaging.app.actor.dialog.command_handler;

import java.util.Objects;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.RemoveMessageCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageRemovedEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.actor.typed.javadsl.ActorContext;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EffectFactories;

public class RemoveMessageDialogCommandHandlerConfigurer implements DialogCommandHandlerConfigurer {

	@Override
	public void configure(
			CommandHandlerWithReplyBuilder<DialogCommand, DialogEvent, DialogState> commandHandlerBuilder,
			ActorContext<DialogCommand> actorContext
	) {
		var log = actorContext.getLog();
		commandHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onCommand(RemoveMessageCommand.class, (state, command) -> {
					var requester = command.requester();
					if (state.isParticipantAbsent(requester)) {
						log.info("Requester of message {} removal {} is not part of the dialog...", command.messageId(), requester);
						return new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(command.replyTo(), v -> StatusReply.error("You are not part of conversation"));
					}
					var messageId = command.messageId();
					var foundMessage = state.findMessageById(messageId);
					if (foundMessage.isEmpty()) {
						log.info("Message {} is currently absent", messageId);
						return new EffectFactories<DialogEvent, DialogState>().none().thenReply(command.replyTo(), v -> StatusReply.ack());
					}
					if (foundMessage.get().isCreatedBy(requester)) {
						log.info("{} removed message {}", requester, messageId);
						return new EffectFactories<DialogEvent, DialogState>()
								.persist(new MessageRemovedEvent(messageId, command.timestamp()))
								.thenReply(command.replyTo(), v -> StatusReply.ack());
					}
					log.warn("{} tried to remove message {} which wasn't sent by him", requester, messageId);
					return new EffectFactories<DialogEvent, DialogState>()
							.none()
							.thenReply(command.replyTo(), v -> StatusReply.error("You cannot remove message this message"));
				});
	}

}
