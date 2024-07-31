package org.instant.messaging.app.actor.dialog.command_handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.command.LeaveConversationCommand;
import org.instant.messaging.app.actor.dialog.event.DialogClosedEvent;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.NewLeaderChosenEvent;
import org.instant.messaging.app.actor.dialog.event.ParticipantLeftEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.junit.jupiter.api.Test;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.EffectFactories;
import akka.persistence.typed.javadsl.ReplyEffect;

class TestLeaveConversationDialogCommandHandlerConfigurer extends BaseDialogCommandHandlerConfigurerTest {

	@SuppressWarnings("unchecked")
	@Test
	void givenParticipantIsStillPresentInDialogAndHeIsNotLeader() {
		UUID participant1 = UUID.randomUUID();
		UUID participant2 = UUID.randomUUID();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		final Instant timestamp = Instant.now();

		when(actorContext.getLog()).thenReturn(LOGGER);

		ReplyEffect<DialogEvent, DialogState> replyEffect = createCommandHandler().apply(
				ActiveDialogState.builder()
						.leader(participant2)
						.participants(Set.of(participant1, participant2))
						.build(),
				LeaveConversationCommand.builder()
						.requester(participant1)
						.timestamp(timestamp)
						.replyTo(replyTo)
						.build());
		assertThat(replyEffect)
				.usingRecursiveComparison()
				.isEqualTo(
						new EffectFactories<DialogEvent, DialogState>()
								.persist(List.of(
										new ParticipantLeftEvent(participant1, timestamp)
								))
								.thenReply(replyTo, v -> StatusReply.ack()));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenParticipantIsStillPresentInDialogAndHeIsLeaderButThereAreOtherParticipantsInChat() {
		UUID participant1 = UUID.randomUUID();
		UUID participant2 = UUID.randomUUID();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		final Instant timestamp = Instant.now();

		when(actorContext.getLog()).thenReturn(LOGGER);

		ReplyEffect<DialogEvent, DialogState> replyEffect = createCommandHandler().apply(
				ActiveDialogState.builder()
						.leader(participant1)
						.participants(Set.of(participant1, participant2))
						.build(),
				LeaveConversationCommand.builder()
						.requester(participant1)
						.timestamp(timestamp)
						.replyTo(replyTo)
						.build());
		assertThat(replyEffect)
				.usingRecursiveComparison()
				.isEqualTo(
						new EffectFactories<DialogEvent, DialogState>()
								.persist(List.of(
										new ParticipantLeftEvent(participant1, timestamp),
										new NewLeaderChosenEvent(participant2, timestamp)
								))
								.thenReply(replyTo, v -> StatusReply.ack()));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenParticipantIsStillPresentInDialogAndHeIsLeaderAndThereAreNoOtherParticipantsInChat() {
		UUID participant1 = UUID.randomUUID();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		final Instant timestamp = Instant.now();

		when(actorContext.getLog()).thenReturn(LOGGER);

		ReplyEffect<DialogEvent, DialogState> replyEffect = createCommandHandler().apply(
				ActiveDialogState.builder()
						.leader(participant1)
						.participants(Set.of(participant1))
						.build(),
				LeaveConversationCommand.builder()
						.requester(participant1)
						.timestamp(timestamp)
						.replyTo(replyTo)
						.build());
		assertThat(replyEffect)
				.usingRecursiveComparison()
				.isEqualTo(
						new EffectFactories<DialogEvent, DialogState>()
								.persist(List.of(
										new ParticipantLeftEvent(participant1, timestamp),
										new DialogClosedEvent(timestamp)
								))
								.thenReply(replyTo, v -> StatusReply.ack()));
	}


	@SuppressWarnings("unchecked")
	@Test
	void givenParticipantIsNotPresentInDialog() {
		UUID participant1 = UUID.randomUUID();
		UUID participant2 = UUID.randomUUID();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		final Instant timestamp = Instant.now();

		when(actorContext.getLog()).thenReturn(LOGGER);

		ReplyEffect<DialogEvent, DialogState> replyEffect = createCommandHandler().apply(
				ActiveDialogState.builder()
						.participants(Set.of(participant2))
						.build(),
				LeaveConversationCommand.builder()
						.requester(participant1)
						.timestamp(timestamp)
						.replyTo(replyTo)
						.build());
		assertThat(replyEffect)
				.usingRecursiveComparison()
				.isEqualTo(
						new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(replyTo, v -> StatusReply.ack()));
	}

	@Override
	protected DialogCommandHandlerConfigurer dialogCommandHandlerConfigurer() {
		return new LeaveConversationDialogCommandHandlerConfigurer();
	}
}