package org.instant.messaging.app.dao.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.domain.DialogDetails;
import org.instant.messaging.app.domain.DialogMessage;

import akka.projection.r2dbc.javadsl.R2dbcSession;

public class DialogRepositoryImpl implements DialogRepository {

	public static final ZoneId UTC = ZoneId.of("UTC");

	@Override
	public CompletionStage<DialogDetails> fetchDialogDetails(R2dbcSession session, String dialogId) {

		var dialogTopicFuture = session.selectOne(
						session.createStatement("select u.dialog_topic as topic from dialogs u where u.dialog_id = $1")
								.bind(0, dialogId),
						row -> row.get("topic", String.class))
				.toCompletableFuture();
		var participantsFuture = session.select(
						session.createStatement("select u.user_id as uid from dialog_participants u where u.dialog_id = $1")
								.bind(0, dialogId),
						row -> UUID.fromString(Objects.requireNonNull(row.get("uid", String.class))))
				.toCompletableFuture();
		var messagesFuture = session.select(
						session.createStatement(
										"select u.message_id as message_id, u.content as content, u.user_id as user_id, u.sent_at as sent_at "
												+ "from dialog_messages u "
												+ "where u.dialog_id = $1")
								.bind(0, dialogId),
						row -> DialogMessage.builder()
								.id(UUID.fromString(Objects.requireNonNull(row.get("message_id", String.class))))
								.from(UUID.fromString(Objects.requireNonNull(row.get("user_id", String.class))))
								.messageContent(Objects.requireNonNull(row.get("content", String.class)))
								.sentAt(String.valueOf(row.get("sent_at", LocalDateTime.class)))
								.build())
				.toCompletableFuture();
		var seenFuture = session.select(
						session.createStatement("select message_id, user_id from dialog_messages_seen where dialog_id = $1")
								.bind(0, dialogId),
						row -> new AbstractMap.SimpleEntry<>(row.get("message_id", String.class), row.get("user_id", String.class)))
				.toCompletableFuture();

		return CompletableFuture.allOf(dialogTopicFuture, participantsFuture, messagesFuture, seenFuture)
				.thenApply(v -> dialogTopicFuture.join()
						.map(dialogTopic -> {
							return DialogDetails.builder()
									.topic(dialogTopic)
									.participants(participantsFuture.join())
									.messages(enrichSeenBy(messagesFuture.join(), seenFuture.join()))
									.build();
						})
						.orElseThrow());
	}

	private List<DialogMessage> enrichSeenBy(
			List<DialogMessage> messages,
			List<AbstractMap.SimpleEntry<String, String>> messageIdUserIdPairs
	) {
		var seenByMessageID = messageIdUserIdPairs.stream()
				.collect(Collectors.groupingBy(
						AbstractMap.SimpleEntry::getKey,
						Collectors.mapping(v -> UUID.fromString(v.getValue()), Collectors.toList())));
		return messages.stream()
				.map(v -> v.toBuilder().seenBy(seenByMessageID.getOrDefault(v.id().toString(), List.of())).build())
				.toList();
	}

	@Override
	public CompletionStage<?> createNewDialog(R2dbcSession session, String dialogId, String dialogTopic, List<UUID> participants) {
		return session.update(
				Stream.concat(
								Stream.of(session
										.createStatement(
												"INSERT INTO dialogs (dialog_id, dialog_topic) VALUES ($1, $2)")
										.bind(0, dialogId)
										.bind(1, dialogTopic)),
								participants.stream()
										.map(x -> session.createStatement("INSERT INTO dialog_participants (dialog_id, user_id) VALUES ($1, $2)")
												.bind(0, dialogId)
												.bind(1, x.toString())))
						.toList());
	}

	@Override
	public CompletionStage<?> markAsSeen(
			R2dbcSession session,
			String dialogId,
			UUID messageId,
			UUID userId
	) {
		return session.updateOne(
				session.createStatement("INSERT INTO dialog_messages_seen (message_id, dialog_id, user_id) VALUES ($1, $2, $3)")
						.bind(0, messageId.toString())
						.bind(1, dialogId)
						.bind(2, userId.toString()));
	}

	@Override
	public CompletionStage<?> addMessage(
			R2dbcSession session,
			String dialogId,
			UUID messageId,
			String messageContent,
			UUID from,
			Instant createdAt
	) {
		return session.updateOne(
				session.createStatement("INSERT INTO dialog_messages (message_id, dialog_id, content, user_id, sent_at) "
								+ "values ($1, $2, $3, $4, $5)")
						.bind(0, messageId.toString())
						.bind(1, dialogId)
						.bind(2, messageContent)
						.bind(3, from.toString())
						.bind(4, LocalDateTime.ofInstant(createdAt, UTC)));
	}

	@Override
	public CompletionStage<?> leaveDialog(R2dbcSession session, String dialogId, UUID userId) {
		return session.updateOne(
				session.createStatement("delete from dialog_participants where dialog_id = $1 and user_id = $2")
						.bind(0, dialogId)
						.bind(1, userId.toString()));
	}

	@Override
	public CompletionStage<?> removeMessage(R2dbcSession session, String dialogId, UUID messageId) {
		return session.update(List.of(
				session.createStatement("delete from dialog_messages_seen where dialog_id = $1 and message_id = $2")
						.bind(0, dialogId)
						.bind(1, messageId.toString()),
				session.createStatement("delete from dialog_messages where dialog_id = $1 and message_id = $2")
						.bind(0, dialogId)
						.bind(1, messageId.toString())
		));
	}

	@Override
	public CompletionStage<?> removeDialog(R2dbcSession session, String dialogId) {
		return session.update(List.of(
				session.createStatement("delete from dialogs where dialog_id = $1")
						.bind(0, dialogId),
				session.createStatement("delete from dialog_participants where dialog_id = $1")
						.bind(0, dialogId),
				session.createStatement("delete from dialog_messages where dialog_id = $1")
						.bind(0, dialogId),
				session.createStatement("delete from dialog_messages_seen where dialog_id = $1")
						.bind(0, dialogId)
		));
	}

}
