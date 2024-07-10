package org.instant.messaging.app.dao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.domain.DialogDetails;

import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface DialogRepository {
	CompletionStage<DialogDetails> fetchDialogDetails(R2dbcSession session, String dialogId);

	CompletionStage<?> createNewDialog(R2dbcSession session, String dialogId, String dialogTopic, List<UUID> participants);

	CompletionStage<?> markAsSeen(
			R2dbcSession session,
			String dialogId,
			UUID messageId,
			UUID userId
	);

	CompletionStage<?> addMessage(
			R2dbcSession session,
			String dialogId,
			UUID messageId,
			String messageContent,
			UUID from,
			Instant createdAt
	);

	CompletionStage<?> leaveDialog(R2dbcSession session, String dialogId, UUID userId);

	CompletionStage<?> removeMessage(R2dbcSession session, String dialogId, UUID messageId);

	CompletionStage<?> removeDialog(R2dbcSession session, String dialogId);

}
