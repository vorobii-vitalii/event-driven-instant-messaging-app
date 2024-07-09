package org.instant.messaging.app.grpc.services;

import java.util.concurrent.CompletionStage;

import org.instant.message.app.DialogReadService;
import org.instant.message.app.FetchDialogQuery;
import org.instant.message.app.FetchDialogResponse;

public class DialogReadServiceImpl implements DialogReadService {
	@Override
	public CompletionStage<FetchDialogResponse> fetchDialog(FetchDialogQuery in) {
		// TODO: implement projection
		return null;
	}
}
