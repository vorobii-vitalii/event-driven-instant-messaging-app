package org.instant.messaging.app.grpc.services;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionStage;

import org.instant.message.app.DialogReadService;
import org.instant.message.app.FetchDialogQuery;
import org.instant.message.app.FetchDialogResponse;
import org.instant.message.app.UUID;
import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.domain.DialogMessage;

import akka.actor.typed.ActorSystem;
import akka.projection.r2dbc.javadsl.R2dbcSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DialogReadServiceImpl implements DialogReadService {
	private final ActorSystem<?> actorSystem;
	private final DialogRepository dialogRepository;

	public DialogReadServiceImpl(ActorSystem<?> actorSystem, DialogRepository dialogRepository) {
		this.actorSystem = actorSystem;
		this.dialogRepository = dialogRepository;
	}

	@Override
	public CompletionStage<FetchDialogResponse> fetchDialog(FetchDialogQuery in) {
		var dialogId = in.getDialogId().getValue();
		log.info("Fetching dialog with id = {}", dialogId);
		return R2dbcSession.withSession(actorSystem, session -> {
					return dialogRepository.fetchDialogDetails(session, dialogId);
				})
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
				.build();
	}
}
