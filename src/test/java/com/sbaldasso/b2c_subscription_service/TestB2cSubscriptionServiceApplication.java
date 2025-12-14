package com.sbaldasso.b2c_subscription_service;

import com.sbaldasso.b2c_subscription_service.bootstrap.B2cSubscriptionServiceApplication;
import org.springframework.boot.SpringApplication;

public class TestB2cSubscriptionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(B2cSubscriptionServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
