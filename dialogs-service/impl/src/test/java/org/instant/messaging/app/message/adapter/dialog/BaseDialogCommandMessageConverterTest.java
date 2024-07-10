package org.instant.messaging.app.message.adapter.dialog;

import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

@SuppressWarnings("unchecked")
abstract class BaseDialogCommandMessageConverterTest {
	protected static final Instant TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.SECONDS);
	protected static final UUID REQUESTER = UUID.randomUUID();
	protected static final UUID DIALOG_ID = UUID.randomUUID();
	protected static final UUID MESSAGE_ID = UUID.randomUUID();

	ActorRef<StatusReply<Done>> replyAcceptor = mock(ActorRef.class);

	protected DialogCommand toCommand(DialogKafkaMessage dialogKafkaMessage) {
		return messageConverter().convert(dialogKafkaMessage, replyAcceptor);
	}

	protected abstract DialogCommandMessageConverter messageConverter();

	protected org.instant.message.app.UUID toGrpcUUID(UUID uuid) {
		return org.instant.message.app.UUID.newBuilder().setValue(uuid.toString()).build();
	}
}
