package org.instant.messaging.app.actor.dialog.command_handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageMarkedAsReadEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.Message;
import org.junit.jupiter.api.Test;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.EffectFactories;
import akka.persistence.typed.javadsl.ReplyEffect;

class TestMarkAsReadCommandHandlerConfigurer extends BaseDialogCommandHandlerConfigurerTest {

	@SuppressWarnings("unchecked")
	@Test
	void givenParticipantIsNotPartOfConversation() {
		var participantId1 = UUID.randomUUID();
		var participantId2 = UUID.randomUUID();
		var messageId = UUID.randomUUID();

		when(actorContext.getLog()).thenReturn(LOGGER);
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);

		ReplyEffect<DialogEvent, DialogState> actualReplyEffect = createCommandHandler().apply(
				ActiveDialogState.builder()
						.participants(Set.of(participantId1))
						.messages(Map.of(
								messageId, Message.builder().readers(Set.of()).build()
						))
						.build(),
				MarkAsReadCommand.builder()
						.requester(participantId2)
						.messageId(messageId)
						.replyTo(replyTo)
						.build());

		assertThat(actualReplyEffect)
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>()
						.none()
						.thenReply(replyTo, v -> StatusReply.error("You are not part of conversation")));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenMessageHasBeenAlreadyReadByTheRequester() {
		var participantId1 = UUID.randomUUID();
		var participantId2 = UUID.randomUUID();
		var messageId = UUID.randomUUID();

		when(actorContext.getLog()).thenReturn(LOGGER);
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);

		ReplyEffect<DialogEvent, DialogState> actualReplyEffect = createCommandHandler().apply(
				ActiveDialogState.builder()
						.participants(Set.of(participantId1, participantId2))
						.messages(Map.of(
								messageId, Message.builder().readers(Set.of(participantId2)).build()
						))
						.build(),
				MarkAsReadCommand.builder()
						.requester(participantId2)
						.messageId(messageId)
						.replyTo(replyTo)
						.build());

		assertThat(actualReplyEffect)
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>().none().thenReply(replyTo, v -> StatusReply.ack()));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenMessageHasNotBeenReadByTheRequesterYet() {
		var participantId1 = UUID.randomUUID();
		var participantId2 = UUID.randomUUID();
		var messageId = UUID.randomUUID();

		when(actorContext.getLog()).thenReturn(LOGGER);
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);

		var timestamp = Instant.now();
		ReplyEffect<DialogEvent, DialogState> actualReplyEffect = createCommandHandler().apply(
				ActiveDialogState.builder()
						.participants(Set.of(participantId1, participantId2))
						.messages(Map.of(
								messageId, Message.builder().readers(Set.of()).build()
						))
						.build(),
				MarkAsReadCommand.builder()
						.requester(participantId2)
						.messageId(messageId)
						.replyTo(replyTo)
						.timestamp(timestamp)
						.build());

		assertThat(actualReplyEffect)
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>()
						.persist(new MessageMarkedAsReadEvent(messageId, participantId2, timestamp))
						.thenReply(replyTo, v -> StatusReply.ack()));
	}

	@SuppressWarnings("unchecked")
	@Test
	void giveMessageHasBeenRemoved() {
		var participantId1 = UUID.randomUUID();
		var participantId2 = UUID.randomUUID();
		var messageId = UUID.randomUUID();

		when(actorContext.getLog()).thenReturn(LOGGER);
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);

		var timestamp = Instant.now();
		ReplyEffect<DialogEvent, DialogState> actualReplyEffect = createCommandHandler().apply(
				ActiveDialogState.builder()
						.participants(Set.of(participantId1, participantId2))
						.messages(Map.of())
						.build(),
				MarkAsReadCommand.builder()
						.requester(participantId2)
						.messageId(messageId)
						.replyTo(replyTo)
						.timestamp(timestamp)
						.build());

		assertThat(actualReplyEffect)
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>()
						.none()
						.thenReply(replyTo, v -> StatusReply.error("Message has been removed")));
	}

	@Override
	protected DialogCommandHandlerConfigurer dialogCommandHandlerConfigurer() {
		return new MarkAsReadCommandHandlerConfigurer();
	}
}
