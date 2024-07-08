package org.instant.messaging.app.grpc.services;

import java.util.concurrent.CompletionStage;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.instant.message.app.Acknowledgement;
import org.instant.message.app.DialogKafkaMessage;
import org.instant.message.app.DialogWriteService;
import org.instant.message.app.EndConversation;
import org.instant.message.app.InitializeDialog;
import org.instant.message.app.LeaveConversation;
import org.instant.message.app.MarkAsRead;
import org.instant.message.app.RemoveMessage;
import org.instant.message.app.SendMessage;

import akka.kafka.javadsl.SendProducer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DialogWriteServiceImpl implements DialogWriteService {
	private final SendProducer<String, byte[]> sendProducer;
	private final String dialogCommandsTopic;

	public DialogWriteServiceImpl(SendProducer<String, byte[]> sendProducer, String dialogCommandsTopic) {
		this.sendProducer = sendProducer;
		this.dialogCommandsTopic = dialogCommandsTopic;
	}

	@SneakyThrows
	@Override
	public CompletionStage<Acknowledgement> initializeDialog(InitializeDialog initializeDialog) {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setCommandId(initializeDialog.getCommandId())
				.setInitDialog(DialogKafkaMessage.KafkaInitializeDialog.newBuilder()
						.setDialogId(initializeDialog.getDialogId())
						.setRequester(initializeDialog.getRequester())
						.addAllParticipantsToInvite(initializeDialog.getParticipantsToInviteList())
						.setDialogTopic(initializeDialog.getDialogTopic())
						.setTimestamp(initializeDialog.getTimestamp()))
				.build();
		String dialogId = initializeDialog.getDialogId().getValue();
		return sendMessageToKafka(dialogId, dialogKafkaMessage);
	}

	@Override
	public CompletionStage<Acknowledgement> sendMessage(SendMessage in) {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setCommandId(in.getCommandId())
				.setSendMessage(DialogKafkaMessage.KafkaSendMessage.newBuilder()
						.setDialogId(in.getDialogId())
						.setContent(in.getContent())
						.setFrom(in.getFrom())
						.setMessageId(in.getMessageId())
						.setTimestamp(in.getTimestamp())
						.build())
				.build();
		String dialogId = in.getDialogId().getValue();
		return sendMessageToKafka(dialogId, dialogKafkaMessage);
	}

	@Override
	public CompletionStage<Acknowledgement> removeMessage(RemoveMessage in) {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setCommandId(in.getCommandId())
				.setRemoveMessage(DialogKafkaMessage.KafkaRemoveMessage.newBuilder()
						.setMessageId(in.getMessageId())
						.setDialogId(in.getDialogId())
						.setRequester(in.getRequester())
						.setTimestamp(in.getTimestamp())
						.build())
				.build();
		String dialogId = in.getDialogId().getValue();
		return sendMessageToKafka(dialogId, dialogKafkaMessage);
	}

	@Override
	public CompletionStage<Acknowledgement> markAsRead(MarkAsRead in) {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setCommandId(in.getCommandId())
				.setMarkAsRead(DialogKafkaMessage.KafkaMarkAsRead.newBuilder()
						.setMessageId(in.getMessageId())
						.setDialogId(in.getDialogId())
						.setRequester(in.getRequester())
						.setTimestamp(in.getTimestamp())
						.build())
				.build();
		String dialogId = in.getDialogId().getValue();
		return sendMessageToKafka(dialogId, dialogKafkaMessage);
	}

	@Override
	public CompletionStage<Acknowledgement> leaveConversation(LeaveConversation in) {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setCommandId(in.getCommandId())
				.setLeaveConversation(DialogKafkaMessage.KafkaLeaveConversation.newBuilder()
						.setDialogId(in.getDialogId())
						.setRequester(in.getRequester())
						.setTimestamp(in.getTimestamp())
						.build())
				.build();
		String dialogId = in.getDialogId().getValue();
		return sendMessageToKafka(dialogId, dialogKafkaMessage);
	}

	@Override
	public CompletionStage<Acknowledgement> endConversation(EndConversation in) {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setCommandId(in.getCommandId())
				.setEndConversation(DialogKafkaMessage.KafkaEndConversation.newBuilder()
						.setDialogId(in.getDialogId())
						.setRequester(in.getRequester())
						.setTimestamp(in.getTimestamp())
						.build())
				.build();
		String dialogId = in.getDialogId().getValue();
		return sendMessageToKafka(dialogId, dialogKafkaMessage);
	}

	private CompletionStage<Acknowledgement> sendMessageToKafka(String dialogId, DialogKafkaMessage dialogKafkaMessage) {
		return sendProducer.send(new ProducerRecord<>(dialogCommandsTopic, dialogId, dialogKafkaMessage.toByteArray()))
				.thenApply(recordMetadata -> {
					log.info("Record metadata = {}", recordMetadata);
					return Acknowledgement.newBuilder().setSuccess(true).build();
				})
				.exceptionally(error -> {
					log.warn("Error occurred on send of command", error);
					return Acknowledgement.newBuilder().setSuccess(false).build();
				});
	}

}
