package org.instant.messaging.app.actor.dialog.event_handler;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.event.EventHandlerConfigurer;

public interface DialogEventHandlerConfigurer extends EventHandlerConfigurer<DialogState, DialogEvent> {
}
