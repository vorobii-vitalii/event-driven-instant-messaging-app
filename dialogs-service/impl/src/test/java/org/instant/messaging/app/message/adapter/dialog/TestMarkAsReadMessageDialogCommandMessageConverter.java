package org.instant.messaging.app.message.adapter.dialog;

import static org.assertj.core.api.Assertions.assertThat;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.MarkAsReadCommand;
import org.junit.jupiter.api.Test;

class TestMarkAsReadMessageDialogCommandMessageConverter extends BaseDialogCommandMessageConverterTest {

	@Test
	void convert() {
		DialogKafkaMessage kafkaMessage = DialogKafkaMessage.newBuilder()
				.setMarkAsRead(DialogKafkaMessage.KafkaMarkAsRead.newBuilder()
						.setDialogId(toGrpcUUID(DIALOG_ID))
						.setMessageId(toGrpcUUID(MESSAGE_ID))
						.setRequester(toGrpcUUID(REQUESTER))
						.setTimestamp(TIMESTAMP.toEpochMilli())
						.build())
				.build();
		assertThat(toCommand(kafkaMessage)).isEqualTo(
				MarkAsReadCommand.builder()
						.messageId(MESSAGE_ID)
						.dialogId(DIALOG_ID.toString())
						.requester(REQUESTER)
						.timestamp(TIMESTAMP)
						.replyTo(replyAcceptor)
						.build());
	}

	@Override
	protected DialogCommandMessageConverter messageConverter() {
		return new MarkAsReadMessageDialogCommandMessageConverter();
	}
}
