package com.sbaldasso.b2c_subscription_service.application.service;

import com.sbaldasso.b2c_subscription_service.application.dto.request.CreateSubscriptionRequest;
import com.sbaldasso.b2c_subscription_service.application.dto.response.SubscriptionResponse;
import com.sbaldasso.b2c_subscription_service.domain.event.SubscriptionCanceledEvent;
import com.sbaldasso.b2c_subscription_service.domain.event.SubscriptionCreatedEvent;
import com.sbaldasso.b2c_subscription_service.domain.exception.SubscriptionException;

import com.sbaldasso.b2c_subscription_service.domain.model.Plan;
import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import com.sbaldasso.b2c_subscription_service.domain.model.SubscriptionStatus;
import com.sbaldasso.b2c_subscription_service.domain.model.User;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.PlanRepository;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.SubscriptionRepository;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public SubscriptionResponse createSubscription(Long userId, CreateSubscriptionRequest request) {
        log.info("Creating subscription for user: {} with plan: {}", userId, request.getPlanCode());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.hasActiveSubscription()) {
            throw new SubscriptionException("User already has an active subscription");
        }
        
        Plan plan = planRepository.findByCode(request.getPlanCode())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        
        if (!plan.getActive()) {
            throw new SubscriptionException("Plan is not active");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime trialEnd = plan.hasTrialPeriod() 
                ? now.plusDays(plan.getTrialPeriodInDays()) 
                : null;
        
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(plan.hasTrialPeriod() ? SubscriptionStatus.TRIAL : SubscriptionStatus.ACTIVE)
                .startDate(now)
                .trialEndDate(trialEnd)
                .nextBillingDate(trialEnd != null ? trialEnd : now.plusDays(plan.getBillingCycleInDays()))
                .build();
        
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully with id: {}", savedSubscription.getId());
        
        eventPublisher.publishEvent(new SubscriptionCreatedEvent(savedSubscription));
        
        return SubscriptionResponse.from(savedSubscription);
    }
    
    @Transactional
    public SubscriptionResponse cancelSubscription(Long userId) {
        log.info("Canceling subscription for user: {}", userId);
        
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new SubscriptionException("No active subscription found"));
        
        subscription.cancel();
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        
        log.info("Subscription canceled successfully with id: {}", savedSubscription.getId());
        
        eventPublisher.publishEvent(new SubscriptionCanceledEvent(savedSubscription));
        
        return SubscriptionResponse.from(savedSubscription);
    }
    
    @Transactional
    public void renewSubscription(Long subscriptionId) {
        log.info("Renewing subscription: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("Subscription not found"));
        
        subscription.renew();
        subscriptionRepository.save(subscription);
        
        log.info("Subscription renewed successfully");
    }
    
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionByUserId(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserIdWithPlan(userId)
                .orElseThrow(() -> new SubscriptionException("No subscription found"));
        return SubscriptionResponse.from(subscription);
    }
    
    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getUserSubscriptionHistory(Long userId, Pageable pageable) {
        return subscriptionRepository.findByUserId(userId, pageable)
                .map(SubscriptionResponse::from);
    }
}