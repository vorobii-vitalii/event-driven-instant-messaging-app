package org.instant.messaging.app.actor.dialog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.InitializeDialogCommand;
import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;
import org.instant.messaging.app.actor.dialog.command.RemoveMessageCommand;
import org.instant.messaging.app.actor.dialog.command.SendMessageCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.DialogInitializedEvent;
import org.instant.messaging.app.actor.dialog.event.MessageMarkedAsReadEvent;
import org.instant.messaging.app.actor.dialog.event.MessageRemovedEvent;
import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.ClosedDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.Message;
import org.instant.messaging.app.actor.dialog.state.NotInitializedDialog;

import akka.actor.typed.javadsl.ActorContext;
import akka.pattern.StatusReply;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.RetentionCriteria;

public class DialogActor extends EventSourcedBehaviorWithEnforcedReplies<DialogCommand, DialogEvent, DialogState> {
	private final RetentionCriteria retentionCriteria;
	private final ActorContext<?> actorContext;

	public DialogActor(PersistenceId persistenceId, int performSnapshotAfterEvents, ActorContext<?> actorContext) {
		super(persistenceId);
		this.retentionCriteria = RetentionCriteria.snapshotEvery(performSnapshotAfterEvents);
		this.actorContext = actorContext;
	}

	@Override
	public RetentionCriteria retentionCriteria() {
		return retentionCriteria;
	}

	@Override
	public DialogState emptyState() {
		return new NotInitializedDialog();
	}

	@Override
	public CommandHandlerWithReply<DialogCommand, DialogEvent, DialogState> commandHandler() {
		var commandHandlerBuilder = newCommandHandlerWithReplyBuilder();
		var log = actorContext.getLog();

		// Not initialized state command handlers
		commandHandlerBuilder
				.forStateType(NotInitializedDialog.class)
				.onCommand(InitializeDialogCommand.class, (state, command) -> {
					log.info("Initializing dialog. Command = {}", command);
					return Effect()
							.persist(new DialogInitializedEvent(
									command.requester(),
									command.otherParticipants(),
									command.dialogTopic(),
									command.initializedAt()
							))
							.thenReply(command.replyTo(), ignored -> StatusReply.Ack());
				});

		// Active dialog state command handlers
		commandHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onCommand(MarkAsReadCommand.class, (state, command) -> {
					var requester = command.requester();
					if (state.isParticipantAbsent(requester)) {
						log.info("Requester {} is not part of dialog...", requester);
						return Effect()
								.none()
								.thenReply(command.replyTo(), v -> StatusReply.error("You are not part of conversation"));
					}
					var messageId = command.messageId();
					var message = state.findMessageById(messageId);
					if (message.isPresent()) {
						if (message.get().isReadBy(requester)) {
							log.info("Message {} already read by {}", message, requester);
							return Effect().none().thenReply(command.replyTo(), v -> StatusReply.ack());
						}
						var newEvent = new MessageMarkedAsReadEvent(messageId, requester, command.timestamp());
						log.info("Persisting marked as read event {}", newEvent);
						return Effect().persist(newEvent).thenReply(command.replyTo(), v -> StatusReply.ack());
					} else {
						log.info("Message {} has already been removed", message);
						return Effect().none().thenReply(command.replyTo(), v -> StatusReply.ack());
					}
				})
				.onCommand(SendMessageCommand.class, (state, command) -> {
					var from = command.from();
					if (state.isParticipantAbsent(from)) {
						log.info("Sender of message {} is not part of the dialog...", from);
						return Effect()
								.none()
								.thenReply(command.replyTo(), v -> StatusReply.error("You are not part of conversation"));
					}
					var messageId = command.messageId();
					var foundMessage = state.findMessageById(messageId);
					if (foundMessage.isPresent()) {
						log.info("Dialog already contains message by id = {}", messageId);
						if (foundMessage.get().wasCreatedFromCommand(command)) {
							log.info("Message was created from same command. Sending acknowledgement!");
							return Effect().none().thenReply(command.replyTo(), v -> StatusReply.ack());
						}
						log.warn("Message was created from different command. Sending error!");
						return Effect().none().thenReply(command.replyTo(), v -> StatusReply.error("Something went wrong!"));
					}
					var event = new MessageSentEvent(messageId, from, command.messageContent(), command.timestamp());
					log.info("Persisting message sent event {}", event);
					return Effect().persist(event).thenReply(command.replyTo(), v -> StatusReply.ack());
				})
				.onCommand(RemoveMessageCommand.class, (state, command) -> {
					var requester = command.requester();
					if (state.isParticipantAbsent(requester)) {
						log.info("Requester of message {} removal {} is not part of the dialog...", command.messageId(), requester);
						return Effect()
								.none()
								.thenReply(command.replyTo(), v -> StatusReply.error("You are not part of conversation"));
					}
					var messageId = command.messageId();
					var foundMessage = state.findMessageById(messageId);
					if (foundMessage.isEmpty()) {
						log.info("Message {} is currently absent", messageId);
						return Effect().none().thenReply(command.replyTo(), v -> StatusReply.ack());
					}
					if (Objects.equals(foundMessage.get().from(), requester)) {
						log.info("{} removed message {}", requester, messageId);
						return Effect()
								.persist(new MessageRemovedEvent(messageId, command.timestamp()))
								.thenReply(command.replyTo(), v -> StatusReply.ack());
					}
					log.warn("{} tried to remove message {} which wasn't sent by him", requester, messageId);
					return Effect()
							.none()
							.thenReply(command.replyTo(), v -> StatusReply.error("You cannot remove message this message"));
				});

		// TODO:
		commandHandlerBuilder.forStateType(ClosedDialogState.class);

		return commandHandlerBuilder.build();
	}

	@Override
	public EventHandler<DialogState, DialogEvent> eventHandler() {
		var eventHandlerBuilder = newEventHandlerBuilder();

		// Not initialized dialog state event handlers
		eventHandlerBuilder
				.forStateType(NotInitializedDialog.class)
				.onEvent(DialogInitializedEvent.class, (ignoredState, dialogInitialized) -> {
					Set<UUID> allParticipants = new HashSet<>(dialogInitialized.invitedParticipants());
					allParticipants.add(dialogInitialized.createdBy());
					return new ActiveDialogState(
							dialogInitialized.dialogTopic(),
							new HashMap<>(),
							dialogInitialized.createdBy(),
							allParticipants
					);
				});

		// Active dialog state event handlers
		eventHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onEvent(MessageMarkedAsReadEvent.class, (state, messageMarkedAsRead) -> state.changeMessageById(
						messageMarkedAsRead.messageId(),
						message -> message.markAsReadBy(messageMarkedAsRead.requester())
				))
				.onEvent(MessageSentEvent.class, (state, event) -> state.addMessage(new Message(
						event.messageId(),
						event.from(),
						event.messageContent(),
						event.timestamp(),
						Set.of()
				)))
				.onEvent(MessageRemovedEvent.class, (state, event) -> state.removeMessage(event.messageId()));

		return eventHandlerBuilder.build();
	}

}
