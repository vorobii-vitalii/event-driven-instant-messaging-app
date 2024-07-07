package org.instant.messaging.app.dependency_injection.components;

import java.util.Set;

import org.instant.messaging.app.actor.dialog.command_handler.DialogCommandHandlerConfigurer;
import org.instant.messaging.app.actor.dialog.event_handler.DialogEventHandlerConfigurer;
import org.instant.messaging.app.dependency_injection.modules.DialogCommandHandlerModule;
import org.instant.messaging.app.dependency_injection.modules.DialogEventHandlerModule;

import dagger.Component;

@Component(modules = {DialogCommandHandlerModule.class, DialogEventHandlerModule.class})
public interface DialogActorComponent {
	Set<DialogCommandHandlerConfigurer> dialogCommandHandlerConfigurers();
	Set<DialogEventHandlerConfigurer> dialogEventHandlerConfigurers();
}
