package org.instant.messaging.app.projection.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.dao.DialogMessageAdder;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;
import org.junit.jupiter.api.Test;

class TestAddMessageEventHandler extends BaseDialogProjectionEventHandler<MessageSentEvent> {

	DialogMessageAdder dialogMessageAdder = mock(DialogMessageAdder.class);

	AddMessageEventHandler addMessageEventHandler = new AddMessageEventHandler(dialogMessageAdder);

	@Test
	void handleSubTypeEvent() {
		var event = MessageSentEvent.builder()
				.build();
		when(dialogMessageAdder
				.addMessage(dbcSession, DIALOG_ID, event.messageId(), event.messageContent(), event.from(), event.timestamp()))
				.thenReturn(CompletableFuture.completedFuture(null));
		assertThat(whenHandleEvent(event)).succeedsWithin(TIMEOUT);
	}

	@Override
	protected Class<MessageSentEvent> expectedSubType() {
		return MessageSentEvent.class;
	}

	@Override
	protected CastingProjectionEventHandler<DialogEvent, MessageSentEvent> eventHandler() {
		return addMessageEventHandler;
	}
}
