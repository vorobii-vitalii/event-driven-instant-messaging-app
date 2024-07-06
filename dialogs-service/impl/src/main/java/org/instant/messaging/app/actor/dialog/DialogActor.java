package org.instant.messaging.app.actor.dialog;

import java.util.HashMap;
import java.util.HashSet;
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
import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.ClosedDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.Message;
import org.instant.messaging.app.actor.dialog.state.NotInitializedDialog;

import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
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
				.onCommand(InitializeDialogCommand.class, (ignoredState, initializeDialogCommand) -> {
					log.info("Initializing dialog. Command = {}", initializeDialogCommand);
					return Effect()
							.persist(new DialogInitializedEvent(
									initializeDialogCommand.requester(),
									initializeDialogCommand.otherParticipants(),
									initializeDialogCommand.dialogTopic(),
									initializeDialogCommand.initializedAt()
							));
				});

		commandHandlerBuilder
				.forStateType(ActiveDialogState.class)
				.onCommand(MarkAsReadCommand.class, (activeDialogState, markAsReadCommand) -> {
					var requester = markAsReadCommand.requester();
					if (activeDialogState.isParticipantAbsent(requester)) {
						log.info("Requester {} is not part of dialog...", requester);
						return Effect().unhandled();
					}
					var messageId = markAsReadCommand.messageId();
					var message = activeDialogState.findMessageById(messageId);
					if (message.isPresent()) {
						if (message.get().isReadBy(requester)) {
							log.info("Message {} already read by {}", message, requester);
							return Effect().unhandled();
						}
						var newEvent = new MessageMarkedAsReadEvent(messageId, requester, markAsReadCommand.timestamp());
						log.info("Persisting marked as read event {}", newEvent);
						return Effect().persist(newEvent);
					} else {
						log.info("Message {} has already been removed", message);
						return Effect().none();
					}
				})
				.onCommand(SendMessageCommand.class, (state, sendMessageCommand) -> {
					var from = sendMessageCommand.from();
					if (state.isParticipantAbsent(from)) {
						log.info("Sender of message {} is not part of the dialog...", from);
						return Effect().unhandled();
					}
					var messageId = sendMessageCommand.messageId();
					if (state.containsMessage(messageId)) {
						log.info("Dialog already contains message by id = {}", messageId);
						return Effect().unhandled();
					}
					var event = new MessageSentEvent(messageId, from, sendMessageCommand.messageContent(), sendMessageCommand.timestamp());
					log.info("Persisting message sent event {}", event);
					return Effect().persist(event);
				})
				.onCommand(RemoveMessageCommand.class, (state, removeMessageCommand) -> {
					var requester = removeMessageCommand.requester();
					if (state.isParticipantAbsent(requester)) {
						log.info("Requester of message {} removal {} is not part of the dialog...", removeMessageCommand.messageId(), requester);
						return Effect().unhandled();
					}
					var messageId = removeMessageCommand.messageId();
					if (!state.containsMessage(messageId)) {
						log.info("Message {} is currently absent", messageId);
					}


					return Effect().unhandled();
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
				)));

		return eventHandlerBuilder.build();
	}

}
