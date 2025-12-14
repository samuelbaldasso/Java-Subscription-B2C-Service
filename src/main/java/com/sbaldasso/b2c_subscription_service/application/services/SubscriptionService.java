package com.sbaldasso.b2c_subscription_service.application.services;

import com.sbaldasso.b2c_subscription_service.application.port.out.PlanRepository;
import com.sbaldasso.b2c_subscription_service.application.port.out.SubscriptionRepository;
import com.sbaldasso.b2c_subscription_service.domain.exceptions.BusinessException;
import com.sbaldasso.b2c_subscription_service.domain.model.Plan;
import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SubscriptionService {
    private SubscriptionRepository subscriptionRepository;

    private PlanRepository planRepository;

    @Transactional
    public void subscribe(Long userId, Long planId) throws BusinessException {
        if (subscriptionRepository.existsActiveByUser(userId)) {
            throw new BusinessException("User already has an active subscription");
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("Plan not found"));

        Subscription subscription = Subscription.create(userId, plan);

        subscriptionRepository.save(subscription);

        // eventPublisher.publish(new SubscriptionCreatedEvent(subscription.getId()));
    }

}
