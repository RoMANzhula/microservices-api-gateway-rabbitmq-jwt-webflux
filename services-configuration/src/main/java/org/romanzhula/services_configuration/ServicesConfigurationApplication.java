package org.romanzhula.services_configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ServicesConfigurationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicesConfigurationApplication.class, args);
	}

}
