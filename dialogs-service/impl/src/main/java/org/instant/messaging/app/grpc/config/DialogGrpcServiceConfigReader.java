package org.instant.messaging.app.grpc.config;

import com.typesafe.config.Config;

public class DialogGrpcServiceConfigReader {

	public DialogGrpcServiceConfig readConfig(Config config) {
		return new DialogGrpcServiceConfig(
				config.getString("dialog-grpc-service.interface"),
				config.getInt("dialog-grpc-service.port")
		);
	}

}
