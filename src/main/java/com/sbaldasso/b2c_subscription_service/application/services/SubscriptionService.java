package com.sbaldasso.b2c_subscription_service.application.services;

import com.sbaldasso.b2c_subscription_service.application.port.out.SubscriptionRepository;
import com.sbaldasso.b2c_subscription_service.domain.exceptions.BusinessException;
import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SubscriptionService {
    private SubscriptionRepository subscriptionRepository;

    @Transactional
    public void subscribe(UUID userId, UUID planId) throws BusinessException {
        if (subscriptionRepository.existsActiveByUser(userId)) {
            throw new BusinessException("User already has an active subscription");
        }

        Subscription subscription = Subscription.createTrial(userId, planId, 7);
        subscriptionRepository.save(subscription);

        // eventPublisher.publish(new SubscriptionCreatedEvent(subscription.getId()));
    }

}
