package org.instant.messaging.app.actor.dialog.event_handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.event.MessageSentEvent;
import org.instant.messaging.app.actor.dialog.state.ActiveDialogState;
import org.instant.messaging.app.actor.dialog.state.Message;
import org.junit.jupiter.api.Test;

class TestMessageSentEventHandlerConfigurer extends BaseDialogEventHandlerConfigurerTest {

	@Test
	void configure() {
		var activeDialogState = ActiveDialogState.builder().messages(Map.of()).build();
		var messageId = UUID.randomUUID();
		var from = UUID.randomUUID();
		var messageSentEvent = MessageSentEvent.builder()
				.messageId(messageId)
				.from(from)
				.messageContent("test")
				.build();
		assertThat(eventHandler().apply(activeDialogState, messageSentEvent))
				.isEqualTo(
						ActiveDialogState.builder()
								.messages(Map.of(
										messageId, Message.builder()
												.messageId(messageId)
												.from(from)
												.messageContent("test")
												.readers(Set.of())
												.build()
								))
								.build());
	}

	@Override
	protected DialogEventHandlerConfigurer configurer() {
		return new MessageSentEventHandlerConfigurer();
	}

}
