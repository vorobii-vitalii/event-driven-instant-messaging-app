package org.instant.messaging.app.kafka;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.typesafe.config.Config;

import akka.kafka.ProducerSettings;

public class KafkaProducerSettingsReader {

	public ProducerSettings<String, byte[]> readProducerSettings(Config config) {
		var kafkaProperties = config.getConfig("kafka.producer.properties");
		var kafkaPropertiesMap = Optional.ofNullable(kafkaProperties)
				.map(Config::entrySet)
				.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(Map.Entry::getKey, v -> kafkaProperties.getString(v.getKey())));
		return ProducerSettings.create(
						config.getConfig("akka.kafka.producer"),
						new StringSerializer(),
						new ByteArraySerializer()
				)
				.withBootstrapServers(config.getString("kafka.producer.bootstrapServers"))
				.withProperties(kafkaPropertiesMap);
	}

}
