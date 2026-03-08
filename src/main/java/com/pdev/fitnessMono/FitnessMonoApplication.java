package com.pdev.fitnessMono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FitnessMonoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitnessMonoApplication.class, args);
	}

}
