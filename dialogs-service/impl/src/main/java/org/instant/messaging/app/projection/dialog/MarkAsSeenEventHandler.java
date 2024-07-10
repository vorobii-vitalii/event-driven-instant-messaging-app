package org.instant.messaging.app.projection.dialog;

import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageMarkedAsReadEvent;
import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class MarkAsSeenEventHandler implements CastingProjectionEventHandler<DialogEvent, MessageMarkedAsReadEvent> {
	private final DialogRepository dialogRepository;

	public MarkAsSeenEventHandler(DialogRepository dialogRepository) {
		this.dialogRepository = dialogRepository;
	}

	@Override
	public Class<MessageMarkedAsReadEvent> subType() {
		return MessageMarkedAsReadEvent.class;
	}

	@Override
	public CompletionStage<Done> handleSubTypeEvent(MessageMarkedAsReadEvent subType, String entityId, R2dbcSession session) {
		return dialogRepository.markAsSeen(session, entityId, subType.messageId(), subType.requester())
				.thenApply(v -> Done.done());
	}
}
