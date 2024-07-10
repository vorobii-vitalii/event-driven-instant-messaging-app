package org.instant.messaging.app.actor.dialog.command_handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.NotInitializedDialog;
import org.junit.jupiter.api.Test;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.EffectFactories;

class TestDefaultDialogCommandHandlerConfigurer extends BaseDialogCommandHandlerConfigurerTest {

	@SuppressWarnings("unchecked")
	@Test
	void shouldReturnErrorStatusReplyToCommand() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var replyEffect = createCommandHandler().apply(new NotInitializedDialog(), MarkAsReadCommand.builder().replyTo(replyTo).build());
		assertThat(replyEffect)
				.usingRecursiveComparison()
				.isEqualTo(
						new EffectFactories<DialogEvent, DialogState>()
								.none()
								.thenReply(replyTo, v -> StatusReply.error("Command not applicable in this state")));
	}

	@Override
	protected DialogCommandHandlerConfigurer dialogCommandHandlerConfigurer() {
		return new DefaultDialogCommandHandlerConfigurer();
	}
}
