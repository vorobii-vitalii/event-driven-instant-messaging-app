package org.instant.messaging.app.projection.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.instant.messaging.app.projection.EntityIdExtractor;
import org.junit.jupiter.api.Test;

class TestEntityIdExtractorImpl {

	EntityIdExtractor entityIdExtractor = new EntityIdExtractorImpl();

	@Test
	void extractEntityId() {
		assertThat(entityIdExtractor.extractEntityId("User|123")).isEqualTo("123");
	}
}