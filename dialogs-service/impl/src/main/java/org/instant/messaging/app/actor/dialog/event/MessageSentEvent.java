package org.instant.messaging.app.actor.dialog.event;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record MessageSentEvent(
		UUID messageId,
		UUID from,
		String messageContent,
		Instant timestamp
) implements DialogEvent {
}
