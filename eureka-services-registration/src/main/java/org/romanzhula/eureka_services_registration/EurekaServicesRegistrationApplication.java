package org.romanzhula.eureka_services_registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServicesRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServicesRegistrationApplication.class, args);
	}

}
