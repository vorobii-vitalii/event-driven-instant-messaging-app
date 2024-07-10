package org.instant.messaging.app.projection.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.DialogInitializedEvent;
import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class DialogInitializedEventHandler implements CastingProjectionEventHandler<DialogEvent, DialogInitializedEvent> {
	private final DialogRepository dialogRepository;

	public DialogInitializedEventHandler(DialogRepository dialogRepository) {
		this.dialogRepository = dialogRepository;
	}

	@Override
	public Class<DialogInitializedEvent> subType() {
		return DialogInitializedEvent.class;
	}

	@Override
	public CompletionStage<Done> handleSubTypeEvent(DialogInitializedEvent event, String entityId, R2dbcSession session) {
		List<UUID> participants = new ArrayList<>(event.invitedParticipants());
		participants.add(event.createdBy());
		return dialogRepository.createNewDialog(
				session,
				entityId,
				event.dialogTopic(),
				participants
		).thenApplyAsync(v -> Done.done());
	}
}
