package org.instant.messaging.app.actor.dialog.command;

import java.time.Instant;
import java.util.UUID;

public record SendMessageCommand(
		UUID messageId,
		UUID from,
		String messageContent,
		Instant timestamp
) implements DialogCommand {
}
