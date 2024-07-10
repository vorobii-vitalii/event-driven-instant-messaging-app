package org.instant.messaging.app.message.adapter.dialog;

import static org.assertj.core.api.Assertions.assertThat;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;
import org.junit.jupiter.api.Test;

class TestLeaveConversationMessageDialogCommandMessageConverter extends BaseDialogCommandMessageConverterTest {

	@Test
	void convert() {
		DialogKafkaMessage kafkaMessage = DialogKafkaMessage.newBuilder()
				.setLeaveConversation(DialogKafkaMessage.KafkaLeaveConversation.newBuilder()
						.setDialogId(toGrpcUUID(DIALOG_ID))
						.setRequester(toGrpcUUID(REQUESTER))
						.setTimestamp(TIMESTAMP.toEpochMilli())
						.build())
				.build();
		assertThat(toCommand(kafkaMessage)).isEqualTo(
				MarkAsReadCommand.builder()
						.dialogId(DIALOG_ID.toString())
						.requester(REQUESTER)
						.timestamp(TIMESTAMP)
						.replyTo(replyAcceptor)
						.build());
	}

	@Override
	protected DialogCommandMessageConverter messageConverter() {
		return new LeaveConversationMessageDialogCommandMessageConverter();
	}
}
