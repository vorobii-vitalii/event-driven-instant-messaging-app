package org.instant.messaging.app.dao;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import akka.projection.r2dbc.javadsl.R2dbcSession;

public interface DialogCreator {
	CompletionStage<?> createNewDialog(R2dbcSession session, String dialogId, String dialogTopic, List<UUID> participants);
}
