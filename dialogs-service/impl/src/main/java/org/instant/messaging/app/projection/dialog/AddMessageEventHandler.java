package org.instant.messaging.app.projection.dialog;

import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.dao.DialogMessageAdder;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class AddMessageEventHandler implements CastingProjectionEventHandler<DialogEvent, MessageSentEvent> {
	private final DialogMessageAdder dialogMessageAdder;

	public AddMessageEventHandler(DialogMessageAdder dialogMessageAdder) {
		this.dialogMessageAdder = dialogMessageAdder;
	}

	@Override
	public Class<MessageSentEvent> subType() {
		return MessageSentEvent.class;
	}

	@Override
	public CompletionStage<Done> handleSubTypeEvent(MessageSentEvent subType, String entityId, R2dbcSession session) {
		return dialogMessageAdder
				.addMessage(session, entityId, subType.messageId(), subType.messageContent(), subType.from(), subType.timestamp())
				.thenApply(v -> Done.done());
	}

}
