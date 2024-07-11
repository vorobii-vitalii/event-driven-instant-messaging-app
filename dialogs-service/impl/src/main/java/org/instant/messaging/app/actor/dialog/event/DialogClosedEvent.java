package org.instant.messaging.app.actor.dialog.event;

import java.time.Instant;

import lombok.Builder;

@Builder
public record DialogClosedEvent(Instant timestamp) implements DialogEvent {
}
