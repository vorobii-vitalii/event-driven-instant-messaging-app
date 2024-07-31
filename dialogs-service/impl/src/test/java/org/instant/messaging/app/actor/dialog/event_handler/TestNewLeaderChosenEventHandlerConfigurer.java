package org.instant.messaging.app.actor.dialog.event_handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.instant.messaging.app.actor.dialog.event.NewLeaderChosenEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.junit.jupiter.api.Test;

class TestNewLeaderChosenEventHandlerConfigurer extends BaseDialogEventHandlerConfigurerTest {

	@Test
	void configure() {
		var prevLeader = UUID.randomUUID();
		var newLeader = UUID.randomUUID();
		var newState = eventHandler()
				.apply(ActiveDialogState.builder().leader(prevLeader).build(), new NewLeaderChosenEvent(newLeader, TIMESTAMP));
		assertThat(newState).isEqualTo(ActiveDialogState.builder().leader(newLeader).build());
	}

	@Override
	protected DialogEventHandlerConfigurer configurer() {
		return new NewLeaderChosenEventHandlerConfigurer();
	}
}
