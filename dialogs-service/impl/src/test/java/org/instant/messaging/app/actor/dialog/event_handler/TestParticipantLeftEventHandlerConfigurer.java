package org.instant.messaging.app.actor.dialog.event_handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.event.ParticipantLeftEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.junit.jupiter.api.Test;

class TestParticipantLeftEventHandlerConfigurer extends BaseDialogEventHandlerConfigurerTest {

	@Test
	void configure() {
		var leader = UUID.randomUUID();
		var otherParticipant = UUID.randomUUID();
		var activeDialogState = ActiveDialogState.builder().participants(Set.of(leader, otherParticipant)).leader(leader).build();
		var newState =
				eventHandler().apply(activeDialogState, ParticipantLeftEvent.builder().requester(otherParticipant).timestamp(TIMESTAMP).build());
		assertThat(newState).isEqualTo(ActiveDialogState.builder().participants(Set.of(leader)).leader(leader).build());
	}

	@Override
	protected DialogEventHandlerConfigurer configurer() {
		return new ParticipantLeftEventHandlerConfigurer();
	}
}

