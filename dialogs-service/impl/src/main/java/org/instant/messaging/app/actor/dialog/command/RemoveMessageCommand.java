package org.instant.messaging.app.actor.dialog.command;

import java.time.Instant;
import java.util.UUID;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

public record RemoveMessageCommand(
		UUID messageId,
		UUID requester,
		Instant timestamp,
		ActorRef<StatusReply<Done>> replyTo
) implements DialogCommand {
}
