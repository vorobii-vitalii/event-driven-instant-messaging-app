package org.instant.messaging.app.domain;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record DialogDetails(String topic, List<UUID> participants, List<DialogMessage> messages) {
}
