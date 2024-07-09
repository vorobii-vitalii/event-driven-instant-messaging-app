package org.instant.messaging.app.dependency_injection.components;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.dependency_injection.modules.DialogCommandMessageConvertersModule;
import org.instant.messaging.app.dependency_injection.modules.KafkaDialogEventsProcessModule;
import org.instant.messaging.app.kafka.DialogEventsProcessorInitializer;
import org.instant.messaging.app.message.adapter.MessageAdapter;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import dagger.Component;

@Component(modules = {
		KafkaDialogEventsProcessModule.class,
		DialogCommandMessageConvertersModule.class
})
public interface DialogEventsProcessorComponent {
	DialogEventsProcessorInitializer dialogEventsProcessorInitializer();
	MessageAdapter<ActorRef<StatusReply<Done>>, DialogKafkaMessage, DialogCommand> dialogKafkaMessageAdapter();
}
