package org.instant.messaging.app.dao;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface DialogMessageSeenMarker {
	CompletionStage<?> markAsSeen(
			R2dbcSession session,
			String dialogId,
			UUID messageId,
			UUID userId
	);
}
