package org.instant.messaging.app.grpc.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.instant.message.app.FetchDialogQuery;
import org.instant.message.app.FetchDialogResponse;
import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.domain.DialogDetails;
import org.instant.messaging.app.domain.DialogMessage;
import org.instant.messaging.app.r2dbc.R2dbcSessionExecutor;
import org.junit.jupiter.api.Test;

import akka.projection.r2dbc.javadsl.R2dbcSession;

class TestDialogReadServiceImpl {
	private static final Duration TIMEOUT = Duration.ofSeconds(2);
	private static final String DIALOG_ID = UUID.randomUUID().toString();

	R2dbcSessionExecutor r2dbcSessionExecutor = mock(R2dbcSessionExecutor.class);

	DialogRepository dialogRepository = mock(DialogRepository.class);

	DialogReadServiceImpl dialogReadService = new DialogReadServiceImpl(r2dbcSessionExecutor, dialogRepository);

	R2dbcSession r2dbcSession = mock(R2dbcSession.class);

	@Test
	void fetchDialog() {
		UUID messageId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		DialogDetails dialogDetails = DialogDetails.builder()
				.messages(List.of(
						DialogMessage.builder()
								.id(messageId)
								.from(userId)
								.sentAt("2023/01/01 10:00")
								.seenBy(List.of())
								.messageContent("Message 1")
								.build()
				))
				.participants(List.of(userId))
				.topic("topic")
				.build();
		when(r2dbcSessionExecutor.execute(any())).thenAnswer(v -> {
			Function<R2dbcSession, CompletionStage<?>> function = v.getArgument(0);
			return function.apply(r2dbcSession);
		});
		when(dialogRepository.fetchDialogDetails(r2dbcSession, DIALOG_ID))
				.thenReturn(CompletableFuture.completedFuture(dialogDetails));

		var dialogResponseFuture = dialogReadService.fetchDialog(FetchDialogQuery.newBuilder()
						.setDialogId(toGrpcUUID(UUID.fromString(DIALOG_ID)))
						.build())
				.toCompletableFuture();

		assertThat(dialogResponseFuture).succeedsWithin(TIMEOUT);
		assertThat(dialogResponseFuture.join()).isEqualTo(
				FetchDialogResponse.newBuilder()
						.setDialogTopic("topic")
						.addAllMessages(List.of(
								FetchDialogResponse.DialogMessage.newBuilder()
										.setMessageId(toGrpcUUID(messageId))
										.setFrom(toGrpcUUID(userId))
										.setTimestamp("2023/01/01 10:00")
										.setContent("Message 1")
										.build()
						))
						.addAllParticipants(List.of(toGrpcUUID(userId)))
						.build());

	}

	private org.instant.message.app.UUID toGrpcUUID(java.util.UUID uuid) {
		return org.instant.message.app.UUID.newBuilder().setValue(uuid.toString()).build();
	}

}
