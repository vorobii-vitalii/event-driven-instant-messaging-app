package org.instant.messaging.app.grpc.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.typesafe.config.ConfigFactory;

class TestDialogGrpcServiceConfigReader {

	DialogGrpcServiceConfigReader dialogGrpcServiceConfigReader = new DialogGrpcServiceConfigReader();

	@Test
	void readConfig() {
		var config = ConfigFactory.parseString(
				"""
							{
							   dialog-grpc-service {
								  interface = "0.0.0.0"
								  port = 8101
							   }
							}
						""");

		assertThat(dialogGrpcServiceConfigReader.readConfig(config))
				.isEqualTo(new DialogGrpcServiceConfig("0.0.0.0", 8101));
	}
}
