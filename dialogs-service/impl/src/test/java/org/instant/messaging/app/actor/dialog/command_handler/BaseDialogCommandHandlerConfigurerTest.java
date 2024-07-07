package org.instant.messaging.app.actor.dialog.command_handler;

import static org.mockito.Mockito.mock;

import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.InitializeDialogCommand;
import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.actor.dialog.state.DialogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;

@SuppressWarnings("unchecked")
public abstract class BaseDialogCommandHandlerConfigurerTest {
	protected static final Logger LOGGER = LoggerFactory.getLogger(InitializeDialogCommand.class);

	protected ActorContext<DialogCommand> actorContext = mock(ActorContext.class);

	protected CommandHandlerWithReply<DialogCommand, DialogEvent, DialogState> createCommandHandler() {
		CommandHandlerWithReplyBuilder<DialogCommand, DialogEvent, DialogState> commandHandlerBuilder = new CommandHandlerWithReplyBuilder<>();
		dialogCommandHandlerConfigurer().configure(commandHandlerBuilder, actorContext);
		return commandHandlerBuilder.build();
	}

	protected abstract DialogCommandHandlerConfigurer dialogCommandHandlerConfigurer();

}
