package org.instant.messaging.app.actor.dialog.command;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import akka.serialization.jackson.JsonSerializable;

public interface DialogCommand extends JsonSerializable {
	String dialogId();
	ActorRef<StatusReply<Done>> replyTo();
}
