package org.instant.messaging.app.actor.dialog.event;

import org.instant.message.app.UUID;

public record CloseDialogEvent(UUID closedAt) implements DialogEvent {

}
