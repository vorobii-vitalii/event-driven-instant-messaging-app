package org.instant.messaging.app.actor.dialog.command_handler;

import org.instant.messaging.app.actor.command.CommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;

public interface DialogCommandHandlerConfigurer extends CommandHandlerConfigurer<DialogCommand, DialogEvent, DialogState> {
	default int getPriority() {
		return 0;
	}
}
