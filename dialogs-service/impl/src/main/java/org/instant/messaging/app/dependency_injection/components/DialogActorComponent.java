package org.instant.messaging.app.dependency_injection.components;

import java.util.Set;

import org.instant.messaging.app.actor.dialog.command_handler.DialogCommandHandlerConfigurer;
import org.instant.messaging.app.dependency_injection.modules.DialogCommandHandlerModule;

import dagger.Component;

@Component(modules = {DialogCommandHandlerModule.class})
public interface DialogActorComponent {
	Set<DialogCommandHandlerConfigurer> dialogCommandHandlerConfigurers();
}
