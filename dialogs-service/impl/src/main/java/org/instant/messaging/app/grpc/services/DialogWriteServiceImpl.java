package org.instant.messaging.app.grpc.services;

import java.util.UUID;
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
	public CompletionStage<Acknowledgement> initializeDialog(InitializeDialog in) {
		DialogKafkaMessage dialogKafkaMessage = DialogKafkaMessage.newBuilder()
				.setCommandId(in.getCommandId())
				.setInitDialog(DialogKafkaMessage.KafkaInitializeDialog.newBuilder()
						.setDialogId(in.getDialogId())
						.setRequester(in.getRequester())
						.addAllParticipantsToInvite(in.getParticipantsToInviteList())
						.setDialogTopic(in.getDialogTopic())
						.setTimestamp(in.getTimestamp()))
				.build();
		return sendProducer.send(new ProducerRecord<>(dialogCommandsTopic, in.getDialogId().getValue(), dialogKafkaMessage.toByteArray()))
				.thenApply(recordMetadata -> {
					log.info("Record metadata = {}", recordMetadata);
					return Acknowledgement.newBuilder().setSuccess(true).build();
				})
				.exceptionally(error -> {
					log.warn("Error occurred on send of command", error);
					return Acknowledgement.newBuilder().setSuccess(false).build();
				});
	}

	@Override
	public CompletionStage<Acknowledgement> sendMessage(SendMessage in) {
		return null;
	}

	@Override
	public CompletionStage<Acknowledgement> removeMessage(RemoveMessage in) {
		return null;
	}

	@Override
	public CompletionStage<Acknowledgement> markAsRead(MarkAsRead in) {
		return null;
	}

	@Override
	public CompletionStage<Acknowledgement> leaveConversation(LeaveConversation in) {
		return null;
	}

	@Override
	public CompletionStage<Acknowledgement> endConversation(EndConversation in) {
		return null;
	}
}
