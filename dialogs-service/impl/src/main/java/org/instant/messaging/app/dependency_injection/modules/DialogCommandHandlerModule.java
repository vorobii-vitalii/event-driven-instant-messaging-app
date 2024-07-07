package org.instant.messaging.app.dependency_injection.modules;

import org.instant.messaging.app.actor.dialog.command_handler.DialogCommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.command_handler.InitializeDialogCommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.command_handler.MarkAsReadCommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.command_handler.RemoveMessageDialogCommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.command_handler.SendMessageCommandHandlerConfigurer;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public class DialogCommandHandlerModule {

	@Provides
	@IntoSet
	DialogCommandHandlerConfigurer initializeDialogCommandHandlerConfigurer() {
		return new InitializeDialogCommandHandlerConfigurer();
	}

	@IntoSet
	@Provides
	DialogCommandHandlerConfigurer markAsReadCommandHandlerConfigurer() {
		return new MarkAsReadCommandHandlerConfigurer();
	}

	@IntoSet
	@Provides
	DialogCommandHandlerConfigurer removeMessageDialogCommandHandlerConfigurer() {
		return new RemoveMessageDialogCommandHandlerConfigurer();
	}

	@IntoSet
	@Provides
	DialogCommandHandlerConfigurer sendMessageCommandHandlerConfigurer() {
		return new SendMessageCommandHandlerConfigurer();
	}

}
