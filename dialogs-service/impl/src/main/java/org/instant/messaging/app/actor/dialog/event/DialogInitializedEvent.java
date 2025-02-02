package org.instant.messaging.app.actor.dialog.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record DialogInitializedEvent(
		UUID createdBy,
		List<UUID> invitedParticipants,
		String dialogTopic,
		Instant initializedAt
) implements DialogEvent {
}
