package org.instant.messaging.app.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;

class TestDialogEventsProcessorConfigReader {

	ActorSystem<?> actorSystem = mock(ActorSystem.class);

	DialogEventsProcessorConfigReader dialogEventsProcessorConfigReader = new DialogEventsProcessorConfigReader();

	@Test
	void readConfig() {
		var config = ConfigFactory.parseString(
				"""
							{
							   dialog-events-processor {
								  bootstrapServers = "192.224.21.12:8213"
								  topic = "topic-name"
								  groupId = "group-id"
								  snapshotAfterEvents = 100
								  partitionsFetchTimeout = 5s
								  kafkaProperties = {
								  	auto.offset.reset = "earliest"
								  }
							   }
							}
						""");

		assertThat(dialogEventsProcessorConfigReader.readConfig(config, actorSystem))
				.isEqualTo(DialogEventsProcessorConfig.builder()
						.bootstrapServers("192.224.21.12:8213")
						.topic("topic-name")
						.groupId("group-id")
						.performSnapshotsAfterEvents(100)
						.partitionsFetchTimeout(Duration.ofSeconds(5))
						.kafkaProperties(Map.of(
								"auto.offset.reset", "earliest"
						))
						.actorSystem(actorSystem)
						.build());
	}
}