package org.instant.messaging.app.actor.dialog.command_handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.command.RemoveMessageCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageRemovedEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.Message;
import org.junit.jupiter.api.Test;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.EffectFactories;

class TestRemoveMessageDialogCommandHandlerConfigurer extends BaseDialogCommandHandlerConfigurerTest {

	@SuppressWarnings("unchecked")
	@Test
	void givenRequesterIsNotPartOfDialog() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		var requester = UUID.randomUUID();

		var activeDialogState = ActiveDialogState.builder()
				.participants(Set.of(UUID.randomUUID()))
				.build();
		var messageIdToRemove = UUID.randomUUID();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var removeMessageCommand = RemoveMessageCommand.builder()
				.messageId(messageIdToRemove)
				.replyTo(replyTo)
				.requester(requester)
				.build();
		assertThat(createCommandHandler().apply(activeDialogState, removeMessageCommand))
				.usingRecursiveComparison()
				.isEqualTo(
						new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(replyTo, v -> StatusReply.error("You are not part of conversation")));

	}

	@SuppressWarnings("unchecked")
	@Test
	void givenMessageNotExists() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		var requester = UUID.randomUUID();

		var activeDialogState = ActiveDialogState.builder()
				.participants(Set.of(UUID.randomUUID(), requester))
				.messages(Map.of())
				.build();
		var messageIdToRemove = UUID.randomUUID();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var removeMessageCommand = RemoveMessageCommand.builder()
				.messageId(messageIdToRemove)
				.replyTo(replyTo)
				.requester(requester)
				.build();
		assertThat(createCommandHandler().apply(activeDialogState, removeMessageCommand))
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>().none().thenReply(replyTo, v -> StatusReply.ack()));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenMessageWasSentBySomeoneElse() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		var requester = UUID.randomUUID();
		var messageSender = UUID.randomUUID();
		var messageIdToRemove = UUID.randomUUID();
		var activeDialogState = ActiveDialogState.builder()
				.participants(Set.of(messageSender, requester))
				.messages(Map.of(
						messageIdToRemove, Message.builder().from(messageSender).build()
				))
				.build();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var removeMessageCommand = RemoveMessageCommand.builder()
				.messageId(messageIdToRemove)
				.replyTo(replyTo)
				.requester(requester)
				.build();
		assertThat(createCommandHandler().apply(activeDialogState, removeMessageCommand))
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>()
						.none()
						.thenReply(replyTo, v -> StatusReply.error("You cannot remove message this message")));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenMessageWasSentByRequester() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		var requester = UUID.randomUUID();
		var messageIdToRemove = UUID.randomUUID();
		var activeDialogState = ActiveDialogState.builder()
				.participants(Set.of(UUID.randomUUID(), requester))
				.messages(Map.of(
						messageIdToRemove, Message.builder().from(requester).build()
				))
				.build();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var timestamp = Instant.now();
		var removeMessageCommand = RemoveMessageCommand.builder()
				.messageId(messageIdToRemove)
				.replyTo(replyTo)
				.requester(requester)
				.timestamp(timestamp)
				.build();
		assertThat(createCommandHandler().apply(activeDialogState, removeMessageCommand))
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>()
						.persist(new MessageRemovedEvent(messageIdToRemove, timestamp))
						.thenReply(replyTo, v -> StatusReply.ack()));
	}

	@Override
	protected DialogCommandHandlerConfigurer dialogCommandHandlerConfigurer() {
		return new RemoveMessageDialogCommandHandlerConfigurer();
	}
}
