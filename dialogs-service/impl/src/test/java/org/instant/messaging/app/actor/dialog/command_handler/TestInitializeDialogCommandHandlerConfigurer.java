package org.instant.messaging.app.actor.dialog.command_handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.instant.messaging.app.actor.dialog.command.InitializeDialogCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.event.DialogInitializedEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.instant.messaging.app.actor.dialog.state.NotInitializedDialog;
import org.junit.jupiter.api.Test;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import akka.persistence.typed.javadsl.EffectFactories;

class TestInitializeDialogCommandHandlerConfigurer extends BaseDialogCommandHandlerConfigurerTest {

	@SuppressWarnings("unchecked")
	@Test
	void configure() {
		when(actorContext.getLog()).thenReturn(LOGGER);
		var commandHandler = createCommandHandler();
		var requester = UUID.randomUUID();
		var otherParticipants = List.of(UUID.randomUUID());
		var initializedAt = Instant.now();
		ActorRef<StatusReply<Done>> replyTo = mock(ActorRef.class);
		var initializeDialogCommand = InitializeDialogCommand.builder()
				.requester(requester)
				.otherParticipants(otherParticipants)
				.dialogTopic("Dialog topic")
				.initializedAt(initializedAt)
				.replyTo(replyTo)
				.build();
		assertThat(commandHandler.apply(new NotInitializedDialog(), initializeDialogCommand))
				.usingRecursiveComparison()
				.isEqualTo(new EffectFactories<DialogEvent, DialogState>()
						.persist(new DialogInitializedEvent(
								requester,
								otherParticipants,
								"Dialog topic",
								initializedAt
						))
						.thenReply(replyTo, ignored -> StatusReply.Ack()));

	}

	@Override
	protected DialogCommandHandlerConfigurer dialogCommandHandlerConfigurer() {
		return new InitializeDialogCommandHandlerConfigurer();
	}
}
