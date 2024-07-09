package org.instant.messaging.app.message.adapter.dialog;

import java.time.Instant;
import java.util.UUID;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

// TODO: Register
public class EndConversationMessageDialogCommandMessageConverter implements DialogCommandMessageConverter {
	@Override
	public boolean canHandle(DialogKafkaMessage msg) {
		return msg.getMessageCase() == DialogKafkaMessage.MessageCase.END_CONVERSATION;
	}

	@Override
	public DialogCommand convert(DialogKafkaMessage msg, ActorRef<StatusReply<Done>> context) {
		DialogKafkaMessage.KafkaEndConversation endConversation = msg.getEndConversation();
		return MarkAsReadCommand.builder()
				.dialogId(endConversation.getDialogId().getValue())
				.requester(UUID.fromString(endConversation.getRequester().getValue()))
				.timestamp(Instant.ofEpochMilli(endConversation.getTimestamp()))
				.replyTo(context)
				.build();
	}
}
