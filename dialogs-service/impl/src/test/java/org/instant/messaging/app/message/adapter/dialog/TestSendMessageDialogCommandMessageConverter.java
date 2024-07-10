package org.instant.messaging.app.message.adapter.dialog;

import static org.assertj.core.api.Assertions.assertThat;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.SendMessageCommand;
import org.junit.jupiter.api.Test;

class TestSendMessageDialogCommandMessageConverter extends BaseDialogCommandMessageConverterTest {

	@Test
	void convert() {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setSendMessage(DialogKafkaMessage.KafkaSendMessage.newBuilder()
						.setDialogId(toGrpcUUID(DIALOG_ID))
						.setFrom(toGrpcUUID(REQUESTER))
						.setMessageId(toGrpcUUID(MESSAGE_ID))
						.setContent("Message content")
						.setTimestamp(TIMESTAMP.toEpochMilli())
						.build())
				.build();
		assertThat(toCommand(dialogKafkaMessage)).isEqualTo(
				SendMessageCommand.builder()
						.dialogId(DIALOG_ID.toString())
						.messageContent("Message content")
						.from(REQUESTER)
						.messageId(MESSAGE_ID)
						.timestamp(TIMESTAMP)
						.replyTo(replyAcceptor)
						.build());
	}

	@Override
	protected DialogCommandMessageConverter messageConverter() {
		return new SendMessageDialogCommandMessageConverter();
	}
}
