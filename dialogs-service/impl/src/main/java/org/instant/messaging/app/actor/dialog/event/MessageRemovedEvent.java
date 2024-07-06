package org.instant.messaging.app.actor.dialog.event;

import java.time.Instant;
import java.util.UUID;

public record MessageRemovedEvent(UUID messageId, Instant timestamp) implements DialogEvent {
}
