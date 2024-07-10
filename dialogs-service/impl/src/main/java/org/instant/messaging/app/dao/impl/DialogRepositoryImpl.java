package org.instant.messaging.app.dao.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.domain.DialogDetails;
import org.instant.messaging.app.domain.DialogMessage;

import akka.projection.r2dbc.javadsl.R2dbcSession;

// TODO: Register
public class DialogRepositoryImpl implements DialogRepository {

	@Override
	public CompletionStage<DialogDetails> fetchDialogDetails(R2dbcSession session, UUID dialogId) {
		var dialogIdAsString = dialogId.toString();

		var dialogTopic = session.selectOne(
				session.createStatement("select u.dialog_topic as topic from dialogs u where u.dialog_id = $1")
						.bind(0, dialogIdAsString),
				row -> row.get("topic", String.class));
		var participants = session.select(
				session.createStatement("select u.user_id as uid from dialog_participants u where u.dialog_id = $1")
						.bind(0, dialogIdAsString),
				row -> UUID.fromString(Objects.requireNonNull(row.get("uid", String.class))));
		var messages = session.select(
				session.createStatement(
								"select u.message_id as message_id, u.content as content, u.user_id as user_id, u.sent_at as sent_at "
										+ "from dialog_messages u "
										+ "where u.dialog_id = $1")
						.bind(1, dialogIdAsString),
				row -> DialogMessage.builder()
						.id(UUID.fromString(Objects.requireNonNull(row.get("message_id", String.class))))
						.from(UUID.fromString(Objects.requireNonNull(row.get("user_id", String.class))))
						.messageContent(Objects.requireNonNull(row.get("content", String.class)))
						.sentAt(row.get("sent_at", Instant.class))
						.build());
		// TODO: combine
	}

}
