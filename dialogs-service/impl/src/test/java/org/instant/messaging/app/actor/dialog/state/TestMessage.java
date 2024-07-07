package org.instant.messaging.app.actor.dialog.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TestMessage {

	@Test
	void isCreatedByGivenIdMatches() {
		var expectedId = UUID.randomUUID();
		assertThat(Message.builder().from(expectedId).build().isCreatedBy(expectedId)).isTrue();
	}

	@Test
	void isCreatedByGivenIdNotMatches() {
		var expectedId = UUID.randomUUID();
		var actualId = UUID.randomUUID();
		assertThat(Message.builder().from(actualId).build().isCreatedBy(expectedId)).isFalse();
	}

	@Test
	void isReadByGivenUserIdIsInSet() {
		var expectedId = UUID.randomUUID();
		assertThat(Message.builder().readers(Set.of(expectedId, UUID.randomUUID())).build().isReadBy(expectedId)).isTrue();
	}

	@Test
	void isReadByGivenUserIdIsNotInSet() {
		var expectedId = UUID.randomUUID();
		assertThat(Message.builder().readers(Set.of(UUID.randomUUID())).build().isReadBy(expectedId)).isFalse();
	}

	@Test
	void markAsReadBy() {
		var userId = UUID.randomUUID();
		var messageId = UUID.randomUUID();
		var originalMessage = Message.builder().messageId(messageId).readers(Set.of()).build();
		assertThat(originalMessage.markAsReadBy(userId))
				.isEqualTo(originalMessage.toBuilder().readers(Set.of(userId)).build());
	}

}
