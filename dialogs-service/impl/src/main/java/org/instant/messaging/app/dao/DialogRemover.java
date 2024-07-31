package org.instant.messaging.app.dao;

import java.util.concurrent.CompletionStage;

import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface DialogRemover {
	CompletionStage<?> removeDialog(R2dbcSession session, String dialogId);
}
