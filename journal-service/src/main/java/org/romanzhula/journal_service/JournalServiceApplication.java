package org.romanzhula.journal_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class JournalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JournalServiceApplication.class, args);
	}

}
