package org.instant.messaging.app.projection.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.instant.messaging.app.actor.dialog.event.DialogClosedEvent;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.dao.DialogRemover;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;
import org.junit.jupiter.api.Test;

class TestDialogClosedEventHandler extends BaseDialogProjectionEventHandler<DialogClosedEvent> {
	DialogRemover dialogRemover = mock(DialogRemover.class);

	@Test
	void handleSubTypeEvent() {
		when(dialogRemover.removeDialog(dbcSession, DIALOG_ID)).thenReturn(CompletableFuture.completedFuture(null));
		assertThat(whenHandleEvent(DialogClosedEvent.builder().build())).succeedsWithin(TIMEOUT);
	}

	@Override
	protected Class<DialogClosedEvent> expectedSubType() {
		return DialogClosedEvent.class;
	}

	@Override
	protected CastingProjectionEventHandler<DialogEvent, DialogClosedEvent> eventHandler() {
		return new DialogClosedEventHandler(dialogRemover);
	}
}
