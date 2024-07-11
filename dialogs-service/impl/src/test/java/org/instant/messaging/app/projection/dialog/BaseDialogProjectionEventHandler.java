package org.instant.messaging.app.projection.dialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;
import org.junit.jupiter.api.Test;

import akka.Done;
import akka.projection.r2dbc.javadsl.R2dbcSession;

public abstract class BaseDialogProjectionEventHandler<S extends DialogEvent> {
	protected static final String DIALOG_ID = UUID.randomUUID().toString();
	protected static final Duration TIMEOUT = Duration.ofSeconds(1);

	protected R2dbcSession dbcSession = mock(R2dbcSession.class);

	@Test
	void subType() {
		assertThat(eventHandler().subType()).isEqualTo(expectedSubType());
	}

	protected abstract Class<S> expectedSubType();

	protected CompletionStage<Done> whenHandleEvent(S event) {
		return eventHandler().handleEvent(event, DIALOG_ID, dbcSession);
	}

	protected abstract CastingProjectionEventHandler<DialogEvent, S> eventHandler();

}
