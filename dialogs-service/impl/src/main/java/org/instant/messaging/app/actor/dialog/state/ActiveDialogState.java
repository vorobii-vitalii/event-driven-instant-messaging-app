package org.instant.messaging.app.actor.dialog.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public record ActiveDialogState(
		String dialogTopic,
		Map<UUID, Message> messages,
		UUID createdBy,
		Set<UUID> participants
) implements DialogState {

	public ActiveDialogState addMessage(Message message) {
		Map<UUID, Message> newMessages = new HashMap<>(messages);
		newMessages.put(message.messageId(), message);
		return new ActiveDialogState(dialogTopic, newMessages, createdBy, participants);
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
		return new ActiveDialogState(dialogTopic, newMessages, createdBy, participants);
	}

}
