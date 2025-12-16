package com.sbaldasso.b2c_subscription_service;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestB2cSubscriptionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(B2cSubscriptionServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
