package org.instant.messaging.app.dao;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.instant.messaging.app.domain.DialogDetails;

import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface DialogRepository {
	CompletionStage<DialogDetails> fetchDialogDetails(R2dbcSession session, UUID dialogId);
}
