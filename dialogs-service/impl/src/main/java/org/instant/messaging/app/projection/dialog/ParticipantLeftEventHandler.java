package org.instant.messaging.app.projection.dialog;

import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.ParticipantLeftEvent;
import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class ParticipantLeftEventHandler implements CastingProjectionEventHandler<DialogEvent, ParticipantLeftEvent> {
	private final DialogRepository dialogRepository;

	public ParticipantLeftEventHandler(DialogRepository dialogRepository) {
		this.dialogRepository = dialogRepository;
	}

	@Override
	public Class<ParticipantLeftEvent> subType() {
		return ParticipantLeftEvent.class;
	}

	@Override
	public CompletionStage<Done> handleSubTypeEvent(ParticipantLeftEvent subType, String entityId, R2dbcSession session) {
		return dialogRepository
				.leaveDialog(session, entityId, subType.requester())
				.thenApply(v -> Done.done());
	}

}
