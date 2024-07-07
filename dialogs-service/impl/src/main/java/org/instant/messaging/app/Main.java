package org.instant.messaging.app;

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
			var system = context.getSystem();
			return Behaviors.ignore();
		});
	}

}
