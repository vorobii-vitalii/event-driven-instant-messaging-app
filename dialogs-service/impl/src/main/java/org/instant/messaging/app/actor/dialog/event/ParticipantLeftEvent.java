package org.instant.messaging.app.actor.dialog.event;

import java.time.Instant;
import java.util.UUID;

public record ParticipantLeftEvent(UUID requester, Instant timestamp) implements DialogEvent {
}
