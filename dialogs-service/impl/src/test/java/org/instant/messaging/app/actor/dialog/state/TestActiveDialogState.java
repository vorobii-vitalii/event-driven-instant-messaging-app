package org.instant.messaging.app.actor.dialog.state;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TestActiveDialogState {

	@Test
	void addMessage() {
		var createdBy = UUID.randomUUID();
		var messageId1 = UUID.randomUUID();
		var messageId2 = UUID.randomUUID();
		var message1 = Message.builder()
				.from(createdBy)
				.messageContent("Content 1")
				.build();
		var message2 = Message.builder()
				.messageId(messageId2)
				.messageContent("Content 2")
				.build();
		var activeDialogState = ActiveDialogState.builder()
				.dialogTopic("Dialog topic")
				.createdBy(createdBy)
				.messages(Map.of(
						messageId1, message1
				))
				.participants(Set.of(createdBy))
				.build();
		assertThat(activeDialogState.addMessage(message2))
				.isEqualTo(ActiveDialogState.builder()
						.dialogTopic("Dialog topic")
						.createdBy(createdBy)
						.messages(Map.of(
								messageId1, message1,
								messageId2, message2
						))
						.participants(Set.of(createdBy))
						.build());
	}

	@Test
	void removeMessage() {
		var createdBy = UUID.randomUUID();
		var messageId1 = UUID.randomUUID();
		var messageId2 = UUID.randomUUID();
		var message1 = Message.builder()
				.from(createdBy)
				.messageContent("Content 1")
				.build();
		var message2 = Message.builder()
				.messageId(messageId2)
				.messageContent("Content 2")
				.build();
		var activeDialogState = ActiveDialogState.builder()
				.dialogTopic("Dialog topic")
				.createdBy(createdBy)
				.messages(Map.of(
						messageId1, message1,
						messageId2, message2
				))
				.participants(Set.of(createdBy))
				.build();
		assertThat(activeDialogState.removeMessage(messageId1)).isEqualTo(
				ActiveDialogState.builder()
						.dialogTopic("Dialog topic")
						.createdBy(createdBy)
						.messages(Map.of(
								messageId2, message2
						))
						.participants(Set.of(createdBy))
						.build()
		);
	}

	@Test
	void isParticipantAbsentGivenItsNotInList() {
		var createdBy = UUID.randomUUID();
		var activeDialogState = ActiveDialogState.builder()
				.dialogTopic("Dialog topic")
				.createdBy(createdBy)
				.messages(Map.of())
				.participants(Set.of(createdBy))
				.build();
		assertThat(activeDialogState.isParticipantAbsent(UUID.randomUUID())).isTrue();
	}

	@Test
	void isParticipantAbsentGivenItsInList() {
		var createdBy = UUID.randomUUID();
		var activeDialogState = ActiveDialogState.builder()
				.dialogTopic("Dialog topic")
				.createdBy(createdBy)
				.messages(Map.of())
				.participants(Set.of(createdBy))
				.build();
		assertThat(activeDialogState.isParticipantAbsent(createdBy)).isFalse();
	}

	@Test
	void findMessageByIdGivenSuchMessageExists() {
		var createdBy = UUID.randomUUID();
		var messageId1 = UUID.randomUUID();
		var messageId2 = UUID.randomUUID();
		var message1 = Message.builder()
				.from(createdBy)
				.messageContent("Content 1")
				.build();
		var message2 = Message.builder()
				.messageId(messageId2)
				.messageContent("Content 2")
				.build();
		var activeDialogState = ActiveDialogState.builder()
				.dialogTopic("Dialog topic")
				.createdBy(createdBy)
				.messages(Map.of(
						messageId1, message1,
						messageId2, message2
				))
				.participants(Set.of(createdBy))
				.build();
		assertThat(activeDialogState.findMessageById(messageId1)).contains(message1);
	}

	@Test
	void findMessageByIdGivenSuchMessageNotExists() {
		var createdBy = UUID.randomUUID();
		var messageId1 = UUID.randomUUID();
		var messageId2 = UUID.randomUUID();
		var message1 = Message.builder()
				.from(createdBy)
				.messageContent("Content 1")
				.build();
		var message2 = Message.builder()
				.messageId(messageId2)
				.messageContent("Content 2")
				.build();
		var activeDialogState = ActiveDialogState.builder()
				.dialogTopic("Dialog topic")
				.createdBy(createdBy)
				.messages(Map.of(
						messageId1, message1,
						messageId2, message2
				))
				.participants(Set.of(createdBy))
				.build();
		assertThat(activeDialogState.findMessageById(UUID.randomUUID())).isEmpty();
	}

}