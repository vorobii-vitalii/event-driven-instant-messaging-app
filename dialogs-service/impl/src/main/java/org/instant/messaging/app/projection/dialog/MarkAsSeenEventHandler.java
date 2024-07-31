package org.instant.messaging.app.projection.dialog;

import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageMarkedAsReadEvent;
import org.instant.messaging.app.dao.DialogMessageSeenMarker;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class MarkAsSeenEventHandler implements CastingProjectionEventHandler<DialogEvent, MessageMarkedAsReadEvent> {
	private final DialogMessageSeenMarker dialogMessageSeenMarker;

	public MarkAsSeenEventHandler(DialogMessageSeenMarker dialogMessageSeenMarker) {
		this.dialogMessageSeenMarker = dialogMessageSeenMarker;
	}

	@Override
	public Class<MessageMarkedAsReadEvent> subType() {
		return MessageMarkedAsReadEvent.class;
	}

	@Override
	public CompletionStage<Done> handleSubTypeEvent(MessageMarkedAsReadEvent subType, String entityId, R2dbcSession session) {
		return dialogMessageSeenMarker.markAsSeen(session, entityId, subType.messageId(), subType.requester())
				.thenApply(v -> Done.done());
	}
}
