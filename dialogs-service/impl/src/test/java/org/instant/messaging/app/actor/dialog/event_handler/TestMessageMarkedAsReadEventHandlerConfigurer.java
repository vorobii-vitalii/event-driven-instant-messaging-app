package org.instant.messaging.app.actor.dialog.event_handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.event.MessageMarkedAsReadEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.Message;
import org.junit.jupiter.api.Test;

class TestMessageMarkedAsReadEventHandlerConfigurer extends BaseDialogEventHandlerConfigurerTest {

	@Test
	void configure() {
		final UUID messageId = UUID.randomUUID();
		final UUID requester = UUID.randomUUID();
		DialogState newState = eventHandler().apply(
				ActiveDialogState.builder().messages(Map.of(messageId, Message.builder().readers(Set.of()).build())).build(),
				MessageMarkedAsReadEvent.builder().messageId(messageId).requester(requester).build());
		assertThat(newState)
				.isEqualTo(ActiveDialogState.builder()
						.messages(Map.of(messageId, Message.builder().readers(Set.of(requester)).build()))
						.build());
	}

	@Override
	protected DialogEventHandlerConfigurer configurer() {
		return new MessageMarkedAsReadEventHandlerConfigurer();
	}
}
