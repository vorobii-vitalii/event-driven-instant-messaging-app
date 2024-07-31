package org.instant.messaging.app.projection.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageMarkedAsReadEvent;
import org.instant.messaging.app.dao.DialogMessageSeenMarker;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;
import org.junit.jupiter.api.Test;

class TestMarkAsSeenEventHandler extends BaseDialogProjectionEventHandler<MessageMarkedAsReadEvent> {

	DialogMessageSeenMarker dialogMessageSeenMarker = mock(DialogMessageSeenMarker.class);

	@Test
	void handleSubTypeEvent() {
		var messageId = UUID.randomUUID();
		var requester = UUID.randomUUID();
		var event = MessageMarkedAsReadEvent.builder()
				.messageId(messageId)
				.requester(requester)
				.build();
		when(dialogMessageSeenMarker.markAsSeen(dbcSession, DIALOG_ID, messageId, requester))
				.thenReturn(CompletableFuture.completedFuture(null));
		assertThat(whenHandleEvent(event)).succeedsWithin(TIMEOUT);
	}

	@Override
	protected Class<MessageMarkedAsReadEvent> expectedSubType() {
		return MessageMarkedAsReadEvent.class;
	}

	@Override
	protected CastingProjectionEventHandler<DialogEvent, MessageMarkedAsReadEvent> eventHandler() {
		return new MarkAsSeenEventHandler(dialogMessageSeenMarker);
	}
}
