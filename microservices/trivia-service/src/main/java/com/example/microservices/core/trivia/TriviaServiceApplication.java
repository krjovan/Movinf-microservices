package com.example.microservices.core.trivia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example")
public class TriviaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TriviaServiceApplication.class, args);
	}

}
