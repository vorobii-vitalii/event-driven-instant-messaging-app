package org.instant.messaging.app.actor.dialog.command_handler;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageMarkedAsReadEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;

import akka.actor.typed.javadsl.ActorContext;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EffectFactories;

public class MarkAsReadCommandHandlerConfigurer implements DialogCommandHandlerConfigurer {
	@Override
	public void configure(
			CommandHandlerWithReplyBuilder<DialogCommand, DialogEvent, DialogState> commandHandlerBuilder,
			ActorContext<DialogCommand> actorContext
	) {
		var log = actorContext.getLog();
		commandHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onCommand(MarkAsReadCommand.class, (state, command) -> {
					var requester = command.requester();
					if (state.isParticipantAbsent(requester)) {
						log.info("Requester {} is not part of dialog...", requester);
						return new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(command.replyTo(), v -> StatusReply.error("You are not part of conversation"));
					}
					var messageId = command.messageId();
					var message = state.findMessageById(messageId);
					if (message.isPresent()) {
						if (message.get().isReadBy(requester)) {
							log.info("Message {} already read by {}", message, requester);
							return new EffectFactories<DialogEvent, DialogState>().none().thenReply(command.replyTo(), v -> StatusReply.ack());
						}
						var newEvent = new MessageMarkedAsReadEvent(messageId, requester, command.timestamp());
						log.info("Persisting marked as read event {}", newEvent);
						return new EffectFactories<DialogEvent, DialogState>().persist(newEvent).thenReply(command.replyTo(), v -> StatusReply.ack());
					} else {
						log.info("Message {} has already been removed", message);
						return new EffectFactories<DialogEvent, DialogState>().none().thenReply(command.replyTo(), v -> StatusReply.ack());
					}
				});
	}
}
