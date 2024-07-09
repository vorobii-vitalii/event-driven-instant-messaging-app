package org.instant.messaging.app.actor.dialog.event_handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.event.MessageRemovedEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.Message;
import org.junit.jupiter.api.Test;

class TestMessageRemovedEventHandlerConfigurer extends BaseDialogEventHandlerConfigurerTest {

	@Test
	void configure() {
		UUID messageId = UUID.randomUUID();
		DialogState newState = eventHandler().apply(
				ActiveDialogState.builder().messages(Map.of(messageId, Message.builder().build())).build(),
				MessageRemovedEvent.builder().messageId(messageId).build());
		assertThat(newState).isEqualTo(ActiveDialogState.builder().messages(Map.of()).build());
	}

	@Override
	protected DialogEventHandlerConfigurer configurer() {
		return new MessageRemovedEventHandlerConfigurer();
	}
}