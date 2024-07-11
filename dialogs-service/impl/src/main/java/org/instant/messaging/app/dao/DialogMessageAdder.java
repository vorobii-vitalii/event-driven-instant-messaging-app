package org.instant.messaging.app.dao;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface DialogMessageAdder {
	CompletionStage<?> addMessage(
			R2dbcSession session,
			String dialogId,
			UUID messageId,
			String messageContent,
			UUID from,
			Instant createdAt
	);
}
