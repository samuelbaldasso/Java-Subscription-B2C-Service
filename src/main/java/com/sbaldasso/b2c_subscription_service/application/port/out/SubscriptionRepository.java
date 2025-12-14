package com.sbaldasso.b2c_subscription_service.application.port.out;

import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsActiveByUser(Long userId);
}
