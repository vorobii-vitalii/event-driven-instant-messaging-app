package org.instant.messaging.app.projection.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageRemovedEvent;
import org.instant.messaging.app.dao.DialogMessageRemover;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;
import org.junit.jupiter.api.Test;

class TestMessageRemovedEventHandler extends BaseDialogProjectionEventHandler<MessageRemovedEvent> {

	DialogMessageRemover dialogMessageRemover = mock(DialogMessageRemover.class);

	@Test
	void handleSubTypeEvent() {
		var messageId = UUID.randomUUID();
		when(dialogMessageRemover.removeMessage(dbcSession, DIALOG_ID, messageId))
				.thenReturn(CompletableFuture.completedFuture(null));
		assertThat(whenHandleEvent(MessageRemovedEvent.builder().messageId(messageId).build())).succeedsWithin(TIMEOUT);
	}

	@Override
	protected Class<MessageRemovedEvent> expectedSubType() {
		return MessageRemovedEvent.class;
	}

	@Override
	protected CastingProjectionEventHandler<DialogEvent, MessageRemovedEvent> eventHandler() {
		return new MessageRemovedEventHandler(dialogMessageRemover);
	}

}