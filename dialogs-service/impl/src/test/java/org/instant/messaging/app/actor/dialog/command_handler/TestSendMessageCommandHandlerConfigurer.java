package org.instant.messaging.app.actor.dialog.command_handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.command.SendMessageCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.Message;
import org.junit.jupiter.api.Test;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.EffectFactories;

class TestSendMessageCommandHandlerConfigurer extends BaseDialogCommandHandlerConfigurerTest {

	@SuppressWarnings("unchecked")
	@Test
	void givenParticipantIsAbsentInConversation() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		UUID requester = UUID.randomUUID();
		UUID messageID = UUID.randomUUID();
		var activeDialogState = ActiveDialogState.builder()
				.participants(Set.of())
				.build();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var sendMessageCommand = SendMessageCommand.builder()
				.from(requester)
				.messageContent("Message")
				.messageId(messageID)
				.replyTo(replyTo)
				.build();
		assertThat(createCommandHandler().apply(activeDialogState, sendMessageCommand))
				.usingRecursiveComparison()
				.isEqualTo(
						new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(replyTo, v -> StatusReply.error("You are not part of conversation")));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenMessageByIdNotFoundInDialog() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		UUID requester = UUID.randomUUID();
		UUID messageID = UUID.randomUUID();
		var activeDialogState = ActiveDialogState.builder()
				.participants(Set.of())
				.messages(Map.of())
				.build();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var timestamp = Instant.now();
		var messageContent = "Message";
		var sendMessageCommand = SendMessageCommand.builder()
				.from(requester)
				.messageContent(messageContent)
				.messageId(messageID)
				.replyTo(replyTo)
				.timestamp(timestamp)
				.build();
		assertThat(createCommandHandler().apply(activeDialogState, sendMessageCommand))
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>()
						.persist(MessageSentEvent.builder()
								.messageId(messageID)
								.from(requester)
								.messageContent(messageContent)
								.timestamp(timestamp)
								.build())
						.thenReply(replyTo, v -> StatusReply.ack()));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenMessageWithSameIdButDifferentParametersAlreadyPresent() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		UUID requester = UUID.randomUUID();
		UUID messageID = UUID.randomUUID();
		var timestamp = Instant.now();
		var messageContent = "Message";
		var activeDialogState = ActiveDialogState.builder()
				.participants(Set.of())
				.messages(Map.of(
						messageID,
						Message.builder()
								.readers(Set.of())
								.timestamp(timestamp)
								.messageContent("Hey 123")
								.from(UUID.randomUUID())
								.build()
				))
				.build();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var sendMessageCommand = SendMessageCommand.builder()
				.from(requester)
				.messageContent(messageContent)
				.messageId(messageID)
				.replyTo(replyTo)
				.timestamp(timestamp)
				.build();
		assertThat(createCommandHandler().apply(activeDialogState, sendMessageCommand))
				.usingRecursiveComparison()
				.isEqualTo(
						new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(replyTo, v -> StatusReply.error("Something went wrong!")));
	}

	@SuppressWarnings("unchecked")
	@Test
	void givenMessageWithSameIdAndSameParametersAlreadyPresent() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		UUID requester = UUID.randomUUID();
		UUID messageID = UUID.randomUUID();
		var timestamp = Instant.now();
		var messageContent = "Message";
		var activeDialogState = ActiveDialogState.builder()
				.participants(Set.of())
				.messages(Map.of(
						messageID,
						Message.builder()
								.readers(Set.of())
								.timestamp(timestamp)
								.messageContent(messageContent)
								.from(requester)
								.build()
				))
				.build();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var sendMessageCommand = SendMessageCommand.builder()
				.from(requester)
				.messageContent(messageContent)
				.messageId(messageID)
				.replyTo(replyTo)
				.timestamp(timestamp)
				.build();
		assertThat(createCommandHandler().apply(activeDialogState, sendMessageCommand))
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>().none().thenReply(replyTo, v -> StatusReply.ack()));
	}

	@Override
	protected DialogCommandHandlerConfigurer dialogCommandHandlerConfigurer() {
		return new SendMessageCommandHandlerConfigurer();
	}
}
