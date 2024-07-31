package org.instant.messaging.app.actor.dialog.event_handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.event.DialogInitializedEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.NotInitializedDialog;
import org.junit.jupiter.api.Test;

class TestDialogInitializedEventHandlerConfigurer extends BaseDialogEventHandlerConfigurerTest {

	@Test
	void shouldCorrectlyInitializeInitialState() {
		UUID createdBy = UUID.randomUUID();
		Instant initializedAt = Instant.now();
		UUID invitedParticipant = UUID.randomUUID();
		var event = DialogInitializedEvent.builder()
				.createdBy(createdBy)
				.dialogTopic("Dialog topic")
				.initializedAt(initializedAt)
				.invitedParticipants(List.of(invitedParticipant))
				.build();
		assertThat(eventHandler().apply(new NotInitializedDialog(), event))
				.isEqualTo(
						new ActiveDialogState(
								"Dialog topic",
								new HashMap<>(),
								createdBy,
								createdBy,
								Set.of(invitedParticipant, createdBy)
						));
	}

	@Override
	protected DialogEventHandlerConfigurer configurer() {
		return new DialogInitializedEventHandlerConfigurer();
	}

}