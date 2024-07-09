package org.instant.messaging.app.message.adapter.dialog;

import java.time.Instant;
import java.util.UUID;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.RemoveMessageCommand;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

// TODO: Register
public class RemoveMessageDialogCommandMessageConverter implements DialogCommandMessageConverter {
	@Override
	public boolean canHandle(DialogKafkaMessage msg) {
		return msg.getMessageCase() == DialogKafkaMessage.MessageCase.REMOVE_MESSAGE;
	}

	@Override
	public DialogCommand convert(DialogKafkaMessage msg, ActorRef<StatusReply<Done>> context) {
		DialogKafkaMessage.KafkaRemoveMessage removeMessage = msg.getRemoveMessage();
		return RemoveMessageCommand.builder()
				.dialogId(removeMessage.getDialogId().getValue())
				.messageId(UUID.fromString(removeMessage.getMessageId().getValue()))
				.requester(UUID.fromString(removeMessage.getRequester().getValue()))
				.timestamp(Instant.ofEpochMilli(removeMessage.getTimestamp()))
				.replyTo(context)
				.build();
	}
}
