package com.victoruk.dicestore;

import com.victoruk.dicestore.common.config.appProperties.AppProperties;
import com.victoruk.dicestore.common.config.openapi.OpenApiProperties;
import com.victoruk.dicestore.infrastructure.cloudService.CloudinaryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableConfigurationProperties({OpenApiProperties.class, AppProperties.class, CloudinaryProperties.class})
public class DicestoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(DicestoreApplication.class, args);
	}


}
