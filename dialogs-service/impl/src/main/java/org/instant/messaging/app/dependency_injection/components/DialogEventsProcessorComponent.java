package org.instant.messaging.app.dependency_injection.components;

import org.instant.messaging.app.dependency_injection.modules.KafkaDialogEventsProcessModule;
import org.instant.messaging.app.kafka.DialogEventsProcessorInitializer;

import dagger.Component;

@Component(modules = KafkaDialogEventsProcessModule.class)
public interface DialogEventsProcessorComponent {
	DialogEventsProcessorInitializer dialogEventsProcessorInitializer();
}
