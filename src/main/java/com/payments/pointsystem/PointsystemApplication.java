package com.payments.pointsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PointsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PointsystemApplication.class, args);
	}

}
