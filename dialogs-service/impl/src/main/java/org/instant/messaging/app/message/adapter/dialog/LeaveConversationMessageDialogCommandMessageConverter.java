package org.instant.messaging.app.message.adapter.dialog;

import java.time.Instant;
import java.util.UUID;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.LeaveConversationCommand;
import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

public class LeaveConversationMessageDialogCommandMessageConverter implements DialogCommandMessageConverter {
	@Override
	public boolean canHandle(DialogKafkaMessage msg) {
		return msg.getMessageCase() == DialogKafkaMessage.MessageCase.LEAVE_CONVERSATION;
	}

	@Override
	public DialogCommand convert(DialogKafkaMessage msg, ActorRef<StatusReply<Done>> context) {
		DialogKafkaMessage.KafkaLeaveConversation leaveConversation = msg.getLeaveConversation();
		return LeaveConversationCommand.builder()
				.dialogId(leaveConversation.getDialogId().getValue())
				.requester(UUID.fromString(leaveConversation.getRequester().getValue()))
				.timestamp(Instant.ofEpochMilli(leaveConversation.getTimestamp()))
				.replyTo(context)
				.build();
	}
}
