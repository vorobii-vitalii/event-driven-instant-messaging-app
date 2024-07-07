package org.instant.messaging.app.dependency_injection.modules;

import org.instant.messaging.app.actor.dialog.event_handler.DialogEventHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.event_handler.DialogInitializedEventHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.event_handler.MessageMarkedAsReadEventHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.event_handler.MessageRemovedEventHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.event_handler.MessageSentEventHandlerConfigurer;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public class DialogEventHandlerModule {

	@Provides
	@IntoSet
	DialogEventHandlerConfigurer dialogInitializedEventHandlerConfigurer() {
		return new DialogInitializedEventHandlerConfigurer();
	}

	@Provides
	@IntoSet
	DialogEventHandlerConfigurer messageMarkedAsReadEventHandlerConfigurer() {
		return new MessageMarkedAsReadEventHandlerConfigurer();
	}

	@Provides
	@IntoSet
	DialogEventHandlerConfigurer messageRemovedEventHandlerConfigurer() {
		return new MessageRemovedEventHandlerConfigurer();
	}

	@Provides
	@IntoSet
	DialogEventHandlerConfigurer messageSentEventHandlerConfigurer() {
		return new MessageSentEventHandlerConfigurer();
	}

}
