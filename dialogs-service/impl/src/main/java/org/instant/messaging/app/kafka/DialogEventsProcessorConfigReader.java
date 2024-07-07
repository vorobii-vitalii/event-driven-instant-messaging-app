package org.instant.messaging.app.kafka;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.typesafe.config.Config;

import akka.actor.typed.ActorSystem;

public class DialogEventsProcessorConfigReader {

	public DialogEventsProcessorConfig readConfig(Config config, ActorSystem<?> actorSystem) {
		var processorConfig = config.getConfig("dialog-events-processor");
		var kafkaProperties = processorConfig.getConfig("kafkaProperties");
		var kafkaPropertiesMap = Optional.ofNullable(kafkaProperties)
				.map(Config::entrySet)
				.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(Map.Entry::getKey, v -> kafkaProperties.getString(v.getKey())));

		return DialogEventsProcessorConfig.builder()
				.bootstrapServers(processorConfig.getString("bootstrapServers"))
				.topic(processorConfig.getString("topic"))
				.groupId(processorConfig.getString("groupId"))
				.performSnapshotsAfterEvents(processorConfig.getInt("snapshotAfterEvents"))
				.partitionsFetchTimeout(processorConfig.getDuration("partitionsFetchTimeout"))
				.kafkaProperties(kafkaPropertiesMap)
				.actorSystem(actorSystem)
				.build();
	}

}
