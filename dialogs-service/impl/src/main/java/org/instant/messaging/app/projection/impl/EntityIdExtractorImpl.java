package org.instant.messaging.app.projection.impl;

import org.instant.messaging.app.projection.EntityIdExtractor;

public class EntityIdExtractorImpl implements EntityIdExtractor {

	@Override
	public String extractEntityId(String persistenceId) {
		return persistenceId.split("\\|")[1];
	}

}
