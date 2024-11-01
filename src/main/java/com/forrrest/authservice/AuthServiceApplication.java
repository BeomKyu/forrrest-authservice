package com.forrrest.authservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@PostConstruct
	public void init(){
		// Set default TimeZone to UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
