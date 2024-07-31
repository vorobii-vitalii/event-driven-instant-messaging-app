package org.instant.messaging.app.dependency_injection.modules;

import java.util.Set;

import org.instant.messaging.app.actor.dialog.command_handler.DialogCommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.event_handler.DialogEventHandlerConfigurer;
import org.instant.messaging.app.kafka.DialogEventsProcessorInitializer;

import dagger.Module;
import dagger.Provides;

@Module(includes = {
		DialogCommandHandlerModule.class,
		DialogEventHandlerModule.class
})
public class KafkaDialogEventsProcessModule {

	@Provides
	public DialogEventsProcessorInitializer kafkaShardedDialogEventsProcessorProcessInitializer(
			Set<DialogCommandHandlerConfigurer> dialogCommandHandlerConfigurers,
			Set<DialogEventHandlerConfigurer> dialogEventHandlerConfigurers
	) {
		return new DialogEventsProcessorInitializer(
				dialogCommandHandlerConfigurers,
				dialogEventHandlerConfigurers
		);
	}
}
