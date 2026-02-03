package com.sbaldasso.b2c_subscription_service.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

        @Bean
        public CircuitBreakerConfig defaultCircuitBreakerConfig() {
                return CircuitBreakerConfig.custom()
                                .failureRateThreshold(50)
                                .waitDurationInOpenState(Duration.ofSeconds(10))
                                .slidingWindowSize(10)
                                .permittedNumberOfCallsInHalfOpenState(3)
                                .build();
        }

        @Bean
        public TimeLimiterConfig defaultTimeLimiterConfig() {
                return TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(10))
                                .build();
        }
}
