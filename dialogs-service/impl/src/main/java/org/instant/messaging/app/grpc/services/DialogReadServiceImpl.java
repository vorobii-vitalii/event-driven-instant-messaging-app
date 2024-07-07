package org.instant.messaging.app.grpc.services;

import java.util.concurrent.CompletionStage;

import org.instant.message.app.grpc.DialogReadService;
import org.instant.message.app.grpc.FetchDialogQuery;
import org.instant.message.app.grpc.FetchDialogResponse;

public class DialogReadServiceImpl implements DialogReadService {
	@Override
	public CompletionStage<FetchDialogResponse> fetchDialog(FetchDialogQuery in) {
		return null;
	}
}
