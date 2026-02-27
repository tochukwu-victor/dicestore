package com.victoruk.dicestore;

import com.victoruk.dicestore.openapi.OpenApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableConfigurationProperties(OpenApiProperties.class)
public class DicestoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(DicestoreApplication.class, args);
	}


}
