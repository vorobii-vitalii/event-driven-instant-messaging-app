package org.instant.messaging.app.actor.dialog.event_handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.instant.messaging.app.actor.dialog.event.DialogClosedEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.ClosedDialogState;
import org.junit.jupiter.api.Test;

class TestDialogClosedEventHandlerConfigurer extends BaseDialogEventHandlerConfigurerTest {

	@Test
	void configure() {
		var newState = eventHandler().apply(ActiveDialogState.builder().build(), new DialogClosedEvent(TIMESTAMP));
		assertThat(newState).isEqualTo(new ClosedDialogState(TIMESTAMP));
	}

	@Override
	protected DialogEventHandlerConfigurer configurer() {
		return new DialogClosedEventHandlerConfigurer();
	}
}
