package org.instant.messaging.app.grpc.services;

import java.util.concurrent.CompletionStage;

import org.instant.message.app.grpc.Acknowledgement;
import org.instant.message.app.grpc.DialogWriteService;
import org.instant.message.app.grpc.EndConversation;
import org.instant.message.app.grpc.InitializeDialog;
import org.instant.message.app.grpc.LeaveConversation;
import org.instant.message.app.grpc.MarkAsRead;
import org.instant.message.app.grpc.RemoveMessage;
import org.instant.message.app.grpc.SendMessage;

public class DialogWriteServiceImpl implements DialogWriteService {
	@Override
	public CompletionStage<Acknowledgement> initializeDialog(InitializeDialog in) {
		return null;
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
