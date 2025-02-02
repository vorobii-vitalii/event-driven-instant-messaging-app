package org.instant.messaging.app.actor.dialog.state;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.command.SendMessageCommand;

import lombok.Builder;

@Builder(toBuilder = true)
public record Message(
		UUID messageId,
		UUID from,
		String messageContent,
		Instant timestamp,
		Set<UUID> readers
) {

	public boolean isCreatedBy(UUID expectedFrom) {
		return Objects.equals(from, expectedFrom);
	}

	public boolean isReadBy(UUID reader) {
		return readers.contains(reader);
	}

	public Message markAsReadBy(UUID newReadBy) {
		var newReaders = new HashSet<>(readers);
		newReaders.add(newReadBy);
		return toBuilder().readers(newReaders).build();
	}

	public boolean wasCreatedFromCommand(SendMessageCommand command) {
		return Objects.equals(messageId, command.messageId())
				&& Objects.equals(from, command.from())
				&& Objects.equals(messageContent, command.messageContent())
				&& Objects.equals(timestamp, command.timestamp());
	}
}
