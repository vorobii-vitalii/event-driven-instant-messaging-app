package org.instant.messaging.app.dependency_injection.modules;

import java.util.Set;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.message.adapter.MessageAdapter;
import org.instant.messaging.app.message.adapter.dialog.DialogCommandMessageConverter;
import org.instant.messaging.app.message.adapter.dialog.InitDialogCommandMessageConverter;
import org.instant.messaging.app.message.adapter.dialog.LeaveConversationMessageDialogCommandMessageConverter;
import org.instant.messaging.app.message.adapter.dialog.MarkAsReadMessageDialogCommandMessageConverter;
import org.instant.messaging.app.message.adapter.dialog.RemoveMessageDialogCommandMessageConverter;
import org.instant.messaging.app.message.adapter.dialog.SendMessageDialogCommandMessageConverter;
import org.instant.messaging.app.message.adapter.impl.MessageAdapterImpl;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public class DialogCommandMessageConvertersModule {

	@Provides
	@IntoSet
	DialogCommandMessageConverter initDialogCommandMessageConverter() {
		return new InitDialogCommandMessageConverter();
	}

	@Provides
	@IntoSet
	DialogCommandMessageConverter leaveConversationMessageDialogCommandMessageConverter() {
		return new LeaveConversationMessageDialogCommandMessageConverter();
	}

	@Provides
	@IntoSet
	DialogCommandMessageConverter markAsReadMessageDialogCommandMessageConverter() {
		return new MarkAsReadMessageDialogCommandMessageConverter();
	}

	@Provides
	@IntoSet
	DialogCommandMessageConverter removeMessageDialogCommandMessageConverter() {
		return new RemoveMessageDialogCommandMessageConverter();
	}

	@Provides
	@IntoSet
	DialogCommandMessageConverter sendMessageDialogCommandMessageConverter() {
		return new SendMessageDialogCommandMessageConverter();
	}

	@Provides
	MessageAdapter<ActorRef<StatusReply<Done>>, DialogKafkaMessage, DialogCommand> dialogKafkaMessageAdapter(
			Set<DialogCommandMessageConverter> dialogCommandMessageConverters
	) {
		return new MessageAdapterImpl<>(dialogCommandMessageConverters);
	}

}
