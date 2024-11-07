package com.forrrest.authservice;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.forrrest.common.security.config.JwtProperties;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.forrrest.authservice",
	"com.forrrest.common"
})
@EnableConfigurationProperties(JwtProperties.class)
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
