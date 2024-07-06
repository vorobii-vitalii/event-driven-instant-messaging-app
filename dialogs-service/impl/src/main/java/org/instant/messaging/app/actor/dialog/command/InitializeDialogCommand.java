package org.instant.messaging.app.actor.dialog.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InitializeDialogCommand(
		UUID requester,
		List<UUID> otherParticipants,
		String dialogTopic,
		Instant initializedAt
) implements DialogCommand {
}
