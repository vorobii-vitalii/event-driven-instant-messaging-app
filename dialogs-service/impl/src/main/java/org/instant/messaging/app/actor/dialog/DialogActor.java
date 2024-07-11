package org.instant.messaging.app.actor.dialog;

import java.util.Collection;
import java.util.Comparator;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command_handler.DialogCommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event_handler.DialogEventHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.NotInitializedDialog;

import akka.actor.typed.javadsl.ActorContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.RetentionCriteria;

public class DialogActor extends EventSourcedBehaviorWithEnforcedReplies<DialogCommand, DialogEvent, DialogState> {
	public static final EntityTypeKey<DialogCommand> ENTITY_KEY = EntityTypeKey.create(DialogCommand.class, "Dialog");

	private final RetentionCriteria retentionCriteria;
	private final ActorContext<DialogCommand> actorContext;
	private final Collection<DialogCommandHandlerConfigurer> dialogCommandHandlerConfigurers;
	private final Collection<DialogEventHandlerConfigurer> dialogEventHandlerConfigurers;

	public DialogActor(
			PersistenceId persistenceId,
			int performSnapshotAfterEvents,
			ActorContext<DialogCommand> actorContext,
			Collection<DialogCommandHandlerConfigurer> dialogCommandHandlerConfigurers,
			Collection<DialogEventHandlerConfigurer> dialogEventHandlerConfigurers
	) {
		super(persistenceId);
		this.retentionCriteria = RetentionCriteria.snapshotEvery(performSnapshotAfterEvents);
		this.actorContext = actorContext;
		this.dialogCommandHandlerConfigurers = dialogCommandHandlerConfigurers;
		this.dialogEventHandlerConfigurers = dialogEventHandlerConfigurers;
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
		dialogCommandHandlerConfigurers
				.stream()
				.sorted(Comparator.comparingInt(DialogCommandHandlerConfigurer::getPriority))
				.forEach(configurer -> configurer.configure(commandHandlerBuilder, actorContext));
		return commandHandlerBuilder.build();
	}

	@Override
	public EventHandler<DialogState, DialogEvent> eventHandler() {
		var eventHandlerBuilder = newEventHandlerBuilder();
		dialogEventHandlerConfigurers.forEach(v -> v.configure(eventHandlerBuilder));
		return eventHandlerBuilder.build();
	}

}
