package org.instant.messaging.app.message.adapter.dialog;

import java.time.Instant;
import java.util.UUID;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;
import org.instant.messaging.app.actor.dialog.command.RemoveMessageCommand;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

// TODO: Register
public class MarkAsReadMessageDialogCommandMessageConverter implements DialogCommandMessageConverter {
	@Override
	public boolean canHandle(DialogKafkaMessage msg) {
		return msg.getMessageCase() == DialogKafkaMessage.MessageCase.MARK_AS_READ;
	}

	@Override
	public DialogCommand convert(DialogKafkaMessage msg, ActorRef<StatusReply<Done>> context) {
		DialogKafkaMessage.KafkaMarkAsRead markAsRead = msg.getMarkAsRead();
		return MarkAsReadCommand.builder()
				.messageId(UUID.fromString(markAsRead.getMessageId().getValue()))
				.dialogId(markAsRead.getDialogId().getValue())
				.requester(UUID.fromString(markAsRead.getRequester().getValue()))
				.timestamp(Instant.ofEpochMilli(markAsRead.getTimestamp()))
				.replyTo(context)
				.build();
	}
}
