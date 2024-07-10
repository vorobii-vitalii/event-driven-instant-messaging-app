package org.instant.messaging.app.message.adapter.dialog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.InitializeDialogCommand;
import org.junit.jupiter.api.Test;

class TestInitDialogCommandMessageConverter extends BaseDialogCommandMessageConverterTest {

	@Test
	void convert() {
		var kafkaDialogMessage = DialogKafkaMessage.newBuilder()
				.setInitDialog(DialogKafkaMessage.KafkaInitializeDialog.newBuilder()
						.setTimestamp(TIMESTAMP.toEpochMilli())
						.setRequester(toGrpcUUID(REQUESTER))
						.setDialogTopic("Dialog topic")
						.setDialogId(toGrpcUUID(DIALOG_ID))
						.build())
				.build();
		assertThat(toCommand(kafkaDialogMessage)).isEqualTo(
				InitializeDialogCommand.builder()
						.replyTo(replyAcceptor)
						.initializedAt(TIMESTAMP)
						.dialogTopic("Dialog topic")
						.otherParticipants(List.of())
						.requester(REQUESTER)
						.dialogId(DIALOG_ID.toString())
						.build()
		);
	}

	@Override
	protected DialogCommandMessageConverter messageConverter() {
		return new InitDialogCommandMessageConverter();
	}
}
