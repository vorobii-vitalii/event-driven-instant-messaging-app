package org.instant.messaging.app.actor.dialog.state;

import java.time.Instant;

public record ClosedDialogState(Instant closedAt, ClosedReason closedReason) implements DialogState {
}
