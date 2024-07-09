package org.instant.messaging.app.message.adapter.dialog;

import java.time.Instant;
import java.util.UUID;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.actor.dialog.command.InitializeDialogCommand;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

public class InitDialogCommandMessageConverter implements DialogCommandMessageConverter {

	@Override
	public boolean canHandle(DialogKafkaMessage msg) {
		return msg.getMessageCase() == DialogKafkaMessage.MessageCase.INIT_DIALOG;
	}

	@Override
	public DialogCommand convert(DialogKafkaMessage msg, ActorRef<StatusReply<Done>> replyAcceptor) {
		var initDialog = msg.getInitDialog();
		return InitializeDialogCommand.builder()
				.replyTo(replyAcceptor)
				.initializedAt(Instant.ofEpochMilli(initDialog.getTimestamp()))
				.dialogTopic(initDialog.getDialogTopic())
				.otherParticipants(
						initDialog.getParticipantsToInviteList()
								.stream()
								.map(v -> UUID.fromString(v.getValue()))
								.toList())
				.requester(UUID.fromString(initDialog.getRequester().getValue()))
				.dialogId(initDialog.getDialogId().getValue())
				.build();
	}

}
