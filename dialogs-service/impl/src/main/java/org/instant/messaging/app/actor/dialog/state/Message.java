package org.instant.messaging.app.actor.dialog.state;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record Message(
		UUID messageId,
		UUID from,
		String messageContent,
		Instant timestamp,
		Set<UUID> readers
) {

	public boolean isReadBy(UUID reader) {
		return readers.contains(reader);
	}

	public Message markAsReadBy(UUID newReadBy) {
		var newReaders = new HashSet<>(readers);
		newReaders.add(newReadBy);
		return new Message(messageId, from, messageContent, timestamp, newReaders);
	}

}
