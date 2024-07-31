package org.instant.messaging.app.actor.dialog.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.concurrent.Immutable;

import lombok.Builder;

@Immutable
@Builder(toBuilder = true)
public record ActiveDialogState(
		String dialogTopic,
		Map<UUID, Message> messages,
		UUID createdBy,
		UUID leader,
		Set<UUID> participants
) implements DialogState {

	public boolean isEmptyConversation() {
		return participants.isEmpty();
	}

	public boolean isLeader(UUID requester) {
		return Objects.equals(requester, leader);
	}

	public ActiveDialogState updateLeader(UUID newLeader) {
		return toBuilder().leader(newLeader).build();
	}

	public ActiveDialogState removeParticipant(UUID participant) {
		var updatedParticipants = new HashSet<>(participants);
		updatedParticipants.remove(participant);
		return toBuilder().participants(updatedParticipants).build();
	}

	public ActiveDialogState addMessage(Message message) {
		Map<UUID, Message> newMessages = new HashMap<>(messages);
		newMessages.put(message.messageId(), message);
		return toBuilder().messages(newMessages).build();
	}

	public ActiveDialogState removeMessage(UUID messageId) {
		Map<UUID, Message> newMessages = new HashMap<>(messages);
		newMessages.remove(messageId);
		return toBuilder().messages(newMessages).build();
	}

	public boolean isParticipantAbsent(UUID participant) {
		return !participants.contains(participant);
	}

	public Optional<Message> findMessageById(UUID messageId) {
		return Optional.ofNullable(messages.get(messageId));
	}

	public boolean containsMessage(UUID messageId) {
		return messages.containsKey(messageId);
	}

	public ActiveDialogState changeMessageById(UUID messageId, Function<Message, Message> changeFunction) {
		if (!containsMessage(messageId)) {
			return this;
		}
		Map<UUID, Message> newMessages = new HashMap<>(messages);
		newMessages.put(messageId, changeFunction.apply(messages.get(messageId)));
		return toBuilder().messages(newMessages).build();
	}

}
