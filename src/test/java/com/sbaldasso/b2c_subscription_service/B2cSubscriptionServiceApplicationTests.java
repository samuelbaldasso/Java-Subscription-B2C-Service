package com.sbaldasso.b2c_subscription_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class B2cSubscriptionServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
