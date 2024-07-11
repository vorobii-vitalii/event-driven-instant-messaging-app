package org.instant.messaging.app.projection.dialog;

import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogClosedEvent;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.dao.DialogRemover;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class DialogClosedEventHandler implements CastingProjectionEventHandler<DialogEvent, DialogClosedEvent> {
	private final DialogRemover dialogRemover;

	public DialogClosedEventHandler(DialogRemover dialogRemover) {
		this.dialogRemover = dialogRemover;
	}

	@Override
	public Class<DialogClosedEvent> subType() {
		return DialogClosedEvent.class;
	}

	@Override
	public CompletionStage<Done> handleSubTypeEvent(DialogClosedEvent subType, String entityId, R2dbcSession session) {
		return dialogRemover.removeDialog(session, entityId).thenApply(v -> Done.done());
	}

}
