package org.instant.messaging.app.message.adapter;

import java.util.Optional;

public interface MessageAdapter<CTX, A, B> {
	Optional<B> adaptMessage(A msg, CTX context);
	boolean isSupported(A msg);
}
