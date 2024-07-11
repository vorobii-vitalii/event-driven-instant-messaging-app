package org.instant.messaging.app.projection.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.DialogInitializedEvent;
import org.instant.messaging.app.dao.DialogCreator;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;
import org.junit.jupiter.api.Test;

class TestDialogInitializedEventHandler extends BaseDialogProjectionEventHandler<DialogInitializedEvent> {

	DialogCreator dialogCreator = mock(DialogCreator.class);

	@Test
	void handleSubTypeEvent() {
		var createdBy = UUID.randomUUID();
		var alex = UUID.randomUUID();
		var bob = UUID.randomUUID();
		var event = DialogInitializedEvent.builder()
				.dialogTopic("Some topic")
				.createdBy(createdBy)
				.invitedParticipants(List.of(alex, bob))
				.build();
		when(dialogCreator.createNewDialog(
				dbcSession,
				DIALOG_ID,
				event.dialogTopic(),
				List.of(alex, bob, createdBy)
		)).thenReturn(CompletableFuture.completedFuture(null));
		assertThat(whenHandleEvent(event)).succeedsWithin(TIMEOUT);
	}

	@Override
	protected Class<DialogInitializedEvent> expectedSubType() {
		return DialogInitializedEvent.class;
	}

	@Override
	protected CastingProjectionEventHandler<DialogEvent, DialogInitializedEvent> eventHandler() {
		return new DialogInitializedEventHandler(dialogCreator);
	}
}
