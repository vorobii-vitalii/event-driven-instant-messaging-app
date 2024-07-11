package org.instant.messaging.app.projection.dialog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.ParticipantLeftEvent;
import org.instant.messaging.app.dao.LeaveDialog;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;

class TestParticipantLeftEventHandler extends BaseDialogProjectionEventHandler<ParticipantLeftEvent> {

	LeaveDialog leaveDialog = mock(LeaveDialog.class);

	@Override
	protected Class<ParticipantLeftEvent> expectedSubType() {
		return ParticipantLeftEvent.class;
	}

	@Override
	protected CastingProjectionEventHandler<DialogEvent, ParticipantLeftEvent> eventHandler() {
		return new ParticipantLeftEventHandler(leaveDialog);
	}

}