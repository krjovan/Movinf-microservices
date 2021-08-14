package com.example.microservices.core.crazycredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example")
public class CrazyCreditServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrazyCreditServiceApplication.class, args);
	}

}
