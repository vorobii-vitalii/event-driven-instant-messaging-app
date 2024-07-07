package org.instant.messaging.app.grpc.services;

import java.util.concurrent.CompletionStage;

import org.instant.message.app.Acknowledgement;
import org.instant.message.app.DialogWriteService;
import org.instant.message.app.EndConversation;
import org.instant.message.app.InitializeDialog;
import org.instant.message.app.LeaveConversation;
import org.instant.message.app.MarkAsRead;
import org.instant.message.app.RemoveMessage;
import org.instant.message.app.SendMessage;

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
