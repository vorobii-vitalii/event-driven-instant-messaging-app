package org.instant.messaging.app.actor.dialog.command_handler;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.SendMessageCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.actor.typed.javadsl.ActorContext;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EffectFactories;

public class SendMessageCommandHandlerConfigurer implements DialogCommandHandlerConfigurer {

	@Override
	public void configure(
			CommandHandlerWithReplyBuilder<DialogCommand, DialogEvent, DialogState> commandHandlerBuilder,
			ActorContext<DialogCommand> actorContext
	) {
		var log = actorContext.getLog();
		commandHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onCommand(SendMessageCommand.class, (state, command) -> {
					var from = command.from();
					if (state.isParticipantAbsent(from)) {
						log.info("Sender of message {} is not part of the dialog...", from);
						return new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(command.replyTo(), v -> StatusReply.error("You are not part of conversation"));
					}
					var messageId = command.messageId();
					var foundMessage = state.findMessageById(messageId);
					if (foundMessage.isPresent()) {
						log.info("Dialog already contains message by id = {}", messageId);
						if (foundMessage.get().wasCreatedFromCommand(command)) {
							log.info("Message was created from same command. Sending acknowledgement!");
							return new EffectFactories<DialogEvent, DialogState>()
									.none()
									.thenReply(command.replyTo(), v -> StatusReply.ack());
						}
						log.warn("Message was created from different command. Sending error!");
						return new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(command.replyTo(), v -> StatusReply.error("Something went wrong!"));
					}
					var event = new MessageSentEvent(messageId, from, command.messageContent(), command.timestamp());
					log.info("Persisting message sent event {}", event);
					return new EffectFactories<DialogEvent, DialogState>().persist(event).thenReply(command.replyTo(), v -> StatusReply.ack());
				});
	}

}
