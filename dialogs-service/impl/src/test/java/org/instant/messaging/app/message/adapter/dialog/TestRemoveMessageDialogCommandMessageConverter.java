package org.instant.messaging.app.message.adapter.dialog;

import static org.assertj.core.api.Assertions.assertThat;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.RemoveMessageCommand;
import org.junit.jupiter.api.Test;

class TestRemoveMessageDialogCommandMessageConverter extends BaseDialogCommandMessageConverterTest {

	@Test
	void convert() {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setRemoveMessage(DialogKafkaMessage.KafkaRemoveMessage.newBuilder()
						.setDialogId(toGrpcUUID(DIALOG_ID))
						.setMessageId(toGrpcUUID(MESSAGE_ID))
						.setRequester(toGrpcUUID(REQUESTER))
						.setTimestamp(TIMESTAMP.toEpochMilli())
						.build())
				.build();
		assertThat(toCommand(dialogKafkaMessage)).isEqualTo(
				RemoveMessageCommand.builder()
						.dialogId(DIALOG_ID.toString())
						.messageId(MESSAGE_ID)
						.requester(REQUESTER)
						.timestamp(TIMESTAMP)
						.replyTo(replyAcceptor)
						.build());
	}

	@Override
	protected DialogCommandMessageConverter messageConverter() {
		return new RemoveMessageDialogCommandMessageConverter();
	}
}
