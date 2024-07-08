package org.instant.messaging.app.kafka;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import akka.actor.typed.ActorSystem;
import akka.kafka.ConsumerSettings;
import lombok.Builder;

@Builder
public record DialogEventsProcessorConfig(
		String bootstrapServers,
		String groupId,
		String topic,
		Map<String, String> kafkaProperties,
		ActorSystem<?> actorSystem,
		Duration partitionsFetchTimeout,
		int performSnapshotsAfterEvents
) {

	public ConsumerSettings<String, byte[]> getConsumerSettings() {
		return ConsumerSettings.apply(actorSystem, new StringDeserializer(), new ByteArrayDeserializer())
				.withBootstrapServers(bootstrapServers)
				.withGroupId(groupId)
				.withProperties(kafkaProperties);
	}

}
