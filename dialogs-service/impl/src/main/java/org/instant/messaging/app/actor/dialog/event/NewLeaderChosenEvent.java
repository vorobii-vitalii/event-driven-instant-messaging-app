package org.instant.messaging.app.actor.dialog.event;

import java.time.Instant;
import java.util.UUID;

public record NewLeaderChosenEvent(UUID newLeader, Instant timestamp) implements DialogEvent {
}
