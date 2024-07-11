package org.instant.messaging.app.projection.dialog;

import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.ParticipantLeftEvent;
import org.instant.messaging.app.dao.LeaveDialog;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public class ParticipantLeftEventHandler implements CastingProjectionEventHandler<DialogEvent, ParticipantLeftEvent> {
	private final LeaveDialog leaveDialog;

	public ParticipantLeftEventHandler(LeaveDialog leaveDialog) {
		this.leaveDialog = leaveDialog;
	}

	@Override
	public Class<ParticipantLeftEvent> subType() {
		return ParticipantLeftEvent.class;
	}

	@Override
	public CompletionStage<Done> handleSubTypeEvent(ParticipantLeftEvent subType, String entityId, R2dbcSession session) {
		return leaveDialog
				.leaveDialog(session, entityId, subType.requester())
				.thenApply(v -> Done.done());
	}

}
