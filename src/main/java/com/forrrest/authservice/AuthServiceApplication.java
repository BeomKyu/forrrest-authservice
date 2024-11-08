package com.forrrest.authservice;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.forrrest.authservice",
	"com.forrrest.common"
})
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
