package com.sbaldasso.b2c_subscription_service.subscription.domain.model;

import com.sbaldasso.b2c_subscription_service.domain.exception.SubscriptionException;
import com.sbaldasso.b2c_subscription_service.domain.model.Plan;
import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import com.sbaldasso.b2c_subscription_service.domain.model.SubscriptionStatus;
import com.sbaldasso.b2c_subscription_service.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionTest {
    
    private Subscription subscription;
    private Plan plan;
    private User user;
    
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();
        
        plan = Plan.builder()
                .id(1L)
                .code("PREMIUM")
                .name("Premium Plan")
                .price(new BigDecimal("29.99"))
                .billingCycleInDays(30)
                .trialPeriodInDays(7)
                .active(true)
                .build();
        
        subscription = Subscription.builder()
                .id(1L)
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.TRIAL)
                .startDate(LocalDateTime.now())
                .trialEndDate(LocalDateTime.now().plusDays(7))
                .build();
    }
    
    @Test
    void shouldBeActiveWhenStatusIsActive() {
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        assertTrue(subscription.isActive());
    }
    
    @Test
    void shouldBeActiveWhenInTrial() {
        subscription.setStatus(SubscriptionStatus.TRIAL);
        assertTrue(subscription.isActive());
    }
    
    @Test
    void shouldNotBeActiveWhenCanceled() {
        subscription.setStatus(SubscriptionStatus.CANCELED);
        assertFalse(subscription.isActive());
    }
    
    @Test
    void shouldBeInTrialWhenTrialNotExpired() {
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setTrialEndDate(LocalDateTime.now().plusDays(3));
        assertTrue(subscription.isInTrial());
    }
    
    @Test
    void shouldActivateSuccessfully() {
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.activate();
        
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertNotNull(subscription.getNextBillingDate());
    }
    
    @Test
    void shouldThrowExceptionWhenActivatingCanceledSubscription() {
        subscription.setStatus(SubscriptionStatus.CANCELED);
        
        assertThrows(SubscriptionException.class, () -> subscription.activate());
    }
    
    @Test
    void shouldCancelSuccessfully() {
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.cancel();
        
        assertEquals(SubscriptionStatus.CANCELED, subscription.getStatus());
        assertNotNull(subscription.getCanceledAt());
    }
    
    @Test
    void shouldThrowExceptionWhenCancelingAlreadyCanceledSubscription() {
        subscription.setStatus(SubscriptionStatus.CANCELED);
        
        assertThrows(SubscriptionException.class, () -> subscription.cancel());
    }
    
    @Test
    void shouldRenewSuccessfully() {
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        LocalDateTime oldBillingDate = LocalDateTime.now();
        subscription.setNextBillingDate(oldBillingDate);
        
        subscription.renew();
        
        assertTrue(subscription.getNextBillingDate().isAfter(oldBillingDate));
    }
}