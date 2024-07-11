package org.instant.messaging.app.dao;

public interface DialogRepository extends DialogMessageAdder,
		DialogMessageRemover,
		DialogRemover,
		DialogCreator,
		DialogDetailsFetcher,
		DialogMessageSeenMarker,
		LeaveDialog {

}
