package org.instant.messaging.app.grpc.services;

import java.util.concurrent.CompletionStage;

import org.instant.message.app.DialogReadService;
import org.instant.message.app.FetchDialogQuery;
import org.instant.message.app.FetchDialogResponse;
import org.instant.message.app.UUID;
import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.domain.DialogMessage;
import org.instant.messaging.app.r2dbc.R2dbcSessionExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DialogReadServiceImpl implements DialogReadService {
	private final R2dbcSessionExecutor r2dbcSessionExecutor;
	private final DialogRepository dialogRepository;

	public DialogReadServiceImpl(R2dbcSessionExecutor r2dbcSessionExecutor, DialogRepository dialogRepository) {
		this.r2dbcSessionExecutor = r2dbcSessionExecutor;
		this.dialogRepository = dialogRepository;
	}

	@Override
	public CompletionStage<FetchDialogResponse> fetchDialog(FetchDialogQuery in) {
		var dialogId = in.getDialogId().getValue();
		log.info("Fetching dialog with id = {}", dialogId);
		return r2dbcSessionExecutor.execute(session -> dialogRepository.fetchDialogDetails(session, dialogId))
				.thenApply(v -> {
					log.info("Mapping dialog details {}", v);
					return FetchDialogResponse.newBuilder()
							.setDialogTopic(v.topic())
							.addAllMessages(v.messages().stream().map(this::toGrpcDialogMessage).toList())
							.addAllParticipants(v.participants().stream().map(this::toGrpcUUID).toList())
							.build();
				});
	}

	private UUID toGrpcUUID(java.util.UUID uuid) {
		return UUID.newBuilder().setValue(uuid.toString()).build();
	}

	private FetchDialogResponse.DialogMessage toGrpcDialogMessage(DialogMessage msg) {
		return FetchDialogResponse.DialogMessage.newBuilder()
				.setMessageId(toGrpcUUID(msg.id()))
				.setFrom(toGrpcUUID(msg.from()))
				.setTimestamp(msg.sentAt())
				.setContent(msg.messageContent())
				.addAllSeen(msg.seenBy().stream().map(java.util.UUID::toString).toList())
				.build();
	}
}
