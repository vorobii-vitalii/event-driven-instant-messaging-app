package org.instant.messaging.app.actor.dialog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command_handler.DialogCommandHandlerConfigurer;
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
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.RetentionCriteria;

public class DialogActor extends EventSourcedBehaviorWithEnforcedReplies<DialogCommand, DialogEvent, DialogState> {
	private final RetentionCriteria retentionCriteria;
	private final ActorContext<DialogCommand> actorContext;
	private final List<DialogCommandHandlerConfigurer> dialogCommandHandlerConfigurers;

	public DialogActor(
			PersistenceId persistenceId,
			int performSnapshotAfterEvents,
			ActorContext<DialogCommand> actorContext,
			List<DialogCommandHandlerConfigurer> dialogCommandHandlerConfigurers
	) {
		super(persistenceId);
		this.retentionCriteria = RetentionCriteria.snapshotEvery(performSnapshotAfterEvents);
		this.actorContext = actorContext;
		this.dialogCommandHandlerConfigurers = dialogCommandHandlerConfigurers;
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
		dialogCommandHandlerConfigurers.forEach(configurer -> configurer.configure(commandHandlerBuilder, actorContext, this::Effect));
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
