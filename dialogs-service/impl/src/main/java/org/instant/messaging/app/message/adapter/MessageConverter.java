package org.instant.messaging.app.message.adapter;

public interface MessageConverter<CTX, A, B> {
	boolean canHandle(A msg);
	B convert(A msg, CTX context);
}
