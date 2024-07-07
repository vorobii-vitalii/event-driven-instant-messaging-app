package org.instant.messaging.app;

import org.instant.messaging.app.dependency_injection.components.DaggerDialogActorComponent;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Main {
	private static final String ACTOR_SYSTEM_NAME = "dialogs-service";

	public static void main(String[] args) {
		ActorSystem.create(guardianBehavior(), ACTOR_SYSTEM_NAME);
	}

	private static Behavior<Object> guardianBehavior() {
		return Behaviors.setup(context -> {

			DaggerDialogActorComponent.create().dialogCommandHandlerConfigurers();

			return Behaviors.ignore();
		});
	}

}
