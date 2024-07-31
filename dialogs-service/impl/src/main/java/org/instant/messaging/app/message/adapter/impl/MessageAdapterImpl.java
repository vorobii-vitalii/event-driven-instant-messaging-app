package org.instant.messaging.app.message.adapter.impl;

import java.util.Collection;
import java.util.Optional;

import org.instant.messaging.app.message.adapter.MessageAdapter;
import org.instant.messaging.app.message.adapter.MessageConverter;

public class MessageAdapterImpl<CTX, A, B> implements MessageAdapter<CTX, A, B> {
	private final Collection<? extends MessageConverter<CTX, A, B>> messageConverters;

	public MessageAdapterImpl(Collection<? extends MessageConverter<CTX, A, B>> messageConverters) {
		this.messageConverters = messageConverters;
	}

	@Override
	public Optional<B> adaptMessage(A msg, CTX context) {
		return messageConverters.stream()
				.filter(v -> v.canHandle(msg))
				.findFirst()
				.map(v -> v.convert(msg, context));
	}

	@Override
	public boolean isSupported(A msg) {
		return messageConverters.stream().anyMatch(v -> v.canHandle(msg));
	}
}
