package org.instant.messaging.app.actor.dialog.command_handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.LeaveConversationCommand;
import org.instant.messaging.app.actor.dialog.event.DialogClosedEvent;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.NewLeaderChosenEvent;
import org.instant.messaging.app.actor.dialog.event.ParticipantLeftEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.ClosedDialogState;
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
					List<DialogEvent> events = new ArrayList<>();
					events.add(new ParticipantLeftEvent(requester, command.timestamp()));
					if (state.participants().size() == 1) {
						events.add(new DialogClosedEvent(command.timestamp()));
					} else if (state.isLeader(requester)) {
						var newLeader = state.participants()
								.stream()
								.filter(v -> !Objects.equals(v, requester))
								.findFirst()
								.orElseThrow();
						events.add(new NewLeaderChosenEvent(newLeader, command.timestamp()));
					}
					return new EffectFactories<DialogEvent, DialogState>()
							.persist(events)
							.thenReply(command.replyTo(), v -> StatusReply.ack());
				});
	}

}
