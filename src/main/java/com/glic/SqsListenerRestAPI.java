package com.glic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the SQS Listener REST API.
 * This class serves as the entry point for the Spring Boot application
 * that listens to AWS SQS messages and processes them through REST API calls.
 */
@SpringBootApplication
@EnableAsync
public class SqsListenerRestAPI {

	/**
	 * Default constructor for SqsListenerRestAPI.
	 * Required by Spring Boot for application instantiation.
	 */
	public SqsListenerRestAPI() {
	}

	/**
	 * Main method that starts the Spring Boot application.
	 *
	 * @param args command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(SqsListenerRestAPI.class, args);
	}

}
