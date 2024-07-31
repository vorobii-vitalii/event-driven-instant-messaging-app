package org.instant.messaging.app.message.adapter.dialog;

import java.time.Instant;
import java.util.UUID;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.SendMessageCommand;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

public class SendMessageDialogCommandMessageConverter implements DialogCommandMessageConverter {
	@Override
	public boolean canHandle(DialogKafkaMessage msg) {
		return msg.getMessageCase() == DialogKafkaMessage.MessageCase.SEND_MESSAGE;
	}

	@Override
	public DialogCommand convert(DialogKafkaMessage msg, ActorRef<StatusReply<Done>> context) {
		DialogKafkaMessage.KafkaSendMessage sendMessage = msg.getSendMessage();
		return SendMessageCommand.builder()
				.dialogId(sendMessage.getDialogId().getValue())
				.messageContent(sendMessage.getContent())
				.from(UUID.fromString(sendMessage.getFrom().getValue()))
				.messageId(UUID.fromString(sendMessage.getMessageId().getValue()))
				.timestamp(Instant.ofEpochMilli(sendMessage.getTimestamp()))
				.replyTo(context)
				.build();
	}
}
