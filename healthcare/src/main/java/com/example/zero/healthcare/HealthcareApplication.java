package com.example.zero.healthcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import com.example.zero.healthcare.config.AirbridgeProperties;
import com.example.zero.healthcare.config.UploadProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties({AirbridgeProperties.class, UploadProperties.class})
public class HealthcareApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthcareApplication.class, args);
	}

}
