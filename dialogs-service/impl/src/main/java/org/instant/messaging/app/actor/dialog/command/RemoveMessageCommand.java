package org.instant.messaging.app.actor.dialog.command;

import java.time.Instant;
import java.util.UUID;

public record RemoveMessageCommand(UUID messageId, UUID requester, Instant timestamp) implements DialogCommand {
}
