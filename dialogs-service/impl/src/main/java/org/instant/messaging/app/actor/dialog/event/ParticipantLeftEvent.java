package org.instant.messaging.app.actor.dialog.event;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ParticipantLeftEvent(UUID requester, Instant timestamp) implements DialogEvent {
}
