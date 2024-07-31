package org.instant.messaging.app.message.adapter.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.instant.messaging.app.message.adapter.MessageConverter;
import org.junit.jupiter.api.Test;

class TestMessageAdapterImpl {

	public static final Object CONTEXT = new Object();
	MessageConverter<Object, Integer, String> converter1 = new MessageConverter<Object, Integer, String>() {
		@Override
		public boolean canHandle(Integer msg) {
			return msg > 0 && msg % 2 == 0;
		}

		@Override
		public String convert(Integer msg, Object context) {
			return "Even " + msg;
		}
	};

	MessageConverter<Object, Integer, String> converter2 = new MessageConverter<Object, Integer, String>() {
		@Override
		public boolean canHandle(Integer msg) {
			return msg > 0 && msg % 2 == 1;
		}

		@Override
		public String convert(Integer msg, Object context) {
			return "Odd " + msg;
		}
	};

	MessageAdapterImpl<Object, Integer, String> messageAdapter = new MessageAdapterImpl<>(List.of(converter1, converter2));

	@Test
	void givenNoStrategySupportIt() {
		assertThat(messageAdapter.adaptMessage(-1, CONTEXT)).isEmpty();
		assertThat(messageAdapter.isSupported(-1)).isFalse();
	}

	@Test
	void givenApplicableForStrategy1() {
		assertThat(messageAdapter.adaptMessage(2, CONTEXT)).contains("Even 2");
		assertThat(messageAdapter.isSupported(2)).isTrue();
	}

	@Test
	void givenApplicableForStrategy2() {
		assertThat(messageAdapter.adaptMessage(3, CONTEXT)).contains("Odd 3");
		assertThat(messageAdapter.isSupported(3)).isTrue();
	}
}
