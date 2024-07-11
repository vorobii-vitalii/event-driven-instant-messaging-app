package org.instant.messaging.app.dependency_injection.components;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.dependency_injection.modules.DialogEventsProjectionModule;
import org.instant.messaging.app.projection.ProjectionEventHandler;

import dagger.Component;

@Component(modules = DialogEventsProjectionModule.class)
public interface DialogEventsProjectionComponent {
	ProjectionEventHandler<DialogEvent> dialogEventsProjectionHandler();
}
