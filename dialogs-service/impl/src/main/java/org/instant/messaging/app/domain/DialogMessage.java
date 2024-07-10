package org.instant.messaging.app.domain;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record DialogMessage(UUID id, UUID from, String messageContent, Instant sentAt) {
}
