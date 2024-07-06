package org.instant.messaging.app.actor.dialog.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

public record InitializeDialogCommand(
		UUID requester,
		List<UUID> otherParticipants,
		String dialogTopic,
		Instant initializedAt,
		ActorRef<StatusReply<Done>> replyTo
) implements DialogCommand {
}
