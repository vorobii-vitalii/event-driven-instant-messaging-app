package org.instant.messaging.app.dependency_injection.modules;

import java.util.Set;

import org.instant.messaging.app.actor.dialog.event.DialogEvent;
import org.instant.messaging.app.dao.DialogCreator;
import org.instant.messaging.app.dao.DialogDetailsFetcher;
import org.instant.messaging.app.dao.DialogMessageAdder;
import org.instant.messaging.app.dao.DialogMessageRemover;
import org.instant.messaging.app.dao.DialogMessageSeenMarker;
import org.instant.messaging.app.dao.DialogRemover;
import org.instant.messaging.app.dao.DialogRepository;
import org.instant.messaging.app.dao.LeaveDialog;
import org.instant.messaging.app.dao.impl.DialogRepositoryImpl;
import org.instant.messaging.app.projection.CastingProjectionEventHandler;
import org.instant.messaging.app.projection.DelegatingCastingProjectionEventHandler;
import org.instant.messaging.app.projection.ProjectionEventHandler;
import org.instant.messaging.app.projection.dialog.AddMessageEventHandler;
import org.instant.messaging.app.projection.dialog.DialogClosedEventHandler;
import org.instant.messaging.app.projection.dialog.DialogInitializedEventHandler;
import org.instant.messaging.app.projection.dialog.MarkAsSeenEventHandler;
import org.instant.messaging.app.projection.dialog.MessageRemovedEventHandler;
import org.instant.messaging.app.projection.dialog.ParticipantLeftEventHandler;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

@Module
public class DialogEventsProjectionModule {

	@Provides
	DialogRepository dialogRepository() {
		return new DialogRepositoryImpl();
	}

	@Provides
	@IntoSet
	CastingProjectionEventHandler<DialogEvent, ? extends DialogEvent> addMessageEventHandler(DialogRepository dialogRepository) {
		return new AddMessageEventHandler(dialogRepository);
	}

	@Provides
	@IntoSet
	CastingProjectionEventHandler<DialogEvent, ? extends DialogEvent> dialogClosedEventHandler(DialogRepository dialogRepository) {
		return new DialogClosedEventHandler(dialogRepository);
	}

	@Provides
	@IntoSet
	CastingProjectionEventHandler<DialogEvent, ? extends DialogEvent> dialogInitializedEventHandler(DialogRepository dialogRepository) {
		return new DialogInitializedEventHandler(dialogRepository);
	}

	@Provides
	@IntoSet
	CastingProjectionEventHandler<DialogEvent, ? extends DialogEvent> markAsSeenEventHandler(DialogRepository dialogRepository) {
		return new MarkAsSeenEventHandler(dialogRepository);
	}

	@Provides
	@IntoSet
	CastingProjectionEventHandler<DialogEvent, ? extends DialogEvent> messageRemovedEventHandler(DialogRepository dialogRepository) {
		return new MessageRemovedEventHandler(dialogRepository);
	}

	@Provides
	@IntoSet
	CastingProjectionEventHandler<DialogEvent, ? extends DialogEvent> participantLeftEventHandler(DialogRepository dialogRepository) {
		return new ParticipantLeftEventHandler(dialogRepository);
	}

	@Provides
	ProjectionEventHandler<DialogEvent> dialogEventProjectionEventHandler(
			Set<CastingProjectionEventHandler<DialogEvent, ? extends DialogEvent>> eventHandlers
	) {
		return new DelegatingCastingProjectionEventHandler<>(eventHandlers);
	}

}
