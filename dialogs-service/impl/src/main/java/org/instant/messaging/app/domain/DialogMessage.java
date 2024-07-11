package org.instant.messaging.app.domain;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder(toBuilder = true)
public record DialogMessage(UUID id, UUID from, String messageContent, String sentAt, List<UUID> seenBy) {
}
