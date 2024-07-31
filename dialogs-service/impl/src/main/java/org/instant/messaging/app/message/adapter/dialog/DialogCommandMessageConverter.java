package org.instant.messaging.app.message.adapter.dialog;

import org.instant.message.app.DialogKafkaMessage;
import org.instant.messaging.app.actor.dialog.command.DialogCommand;
import org.instant.messaging.app.message.adapter.MessageConverter;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;

public interface DialogCommandMessageConverter extends MessageConverter<ActorRef<StatusReply<Done>>, DialogKafkaMessage, DialogCommand> {
}
