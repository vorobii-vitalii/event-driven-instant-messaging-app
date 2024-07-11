package org.instant.messaging.app.projection.dialog;

import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageRemovedEvent;
import org.instant.messaging.app.dao.DialogMessageRemover;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class MessageRemovedEventHandler implements CastingProjectionEventHandler<DialogEvent, MessageRemovedEvent> {
	private final DialogMessageRemover dialogMessageRemover;

	public MessageRemovedEventHandler(DialogMessageRemover dialogMessageRemover) {
		this.dialogMessageRemover = dialogMessageRemover;
	}

	@Override
	public Class<MessageRemovedEvent> subType() {
		return MessageRemovedEvent.class;
	}

	@Override
	public CompletionStage<Done> handleSubTypeEvent(MessageRemovedEvent subType, String entityId, R2dbcSession session) {
		return dialogMessageRemover
				.removeMessage(session, entityId, subType.messageId())
				.thenApply(v -> Done.done());
	}

}
