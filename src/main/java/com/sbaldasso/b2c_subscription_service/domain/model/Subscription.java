package com.sbaldasso.b2c_subscription_service.domain.model;

import com.sbaldasso.b2c_subscription_service.domain.valueobject.SubscriptionStatus;

import java.time.LocalDate;
import java.util.UUID;

public class Subscription {

    private final UUID id;
    private final UUID userId;
    private final UUID planId;

    private SubscriptionStatus status;

    private final LocalDate startDate;
    private LocalDate endDate;

    private final boolean trial;
    private final int billingCycleDays;

    private Subscription(UUID userId, UUID planId, boolean trial, int billingCycleDays) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.planId = planId;
        this.trial = trial;
        this.billingCycleDays = billingCycleDays;

        this.startDate = LocalDate.now();
        this.endDate = startDate.plusDays(billingCycleDays);
        this.status = trial ? SubscriptionStatus.TRIAL : SubscriptionStatus.ACTIVE;
    }

    public static Subscription createTrial(UUID userId, UUID planId, int trialDays) {
        return new Subscription(userId, planId, true, trialDays);
    }

    public static Subscription createPaid(UUID userId, UUID planId, int billingCycleDays) {
        return new Subscription(userId, planId, false, billingCycleDays);
    }

    public void renew() {
        if (status == SubscriptionStatus.CANCELED || status == SubscriptionStatus.EXPIRED) {
            throw new IllegalStateException("Cannot renew canceled or expired subscription");
        }

        this.status = SubscriptionStatus.ACTIVE;
        this.endDate = endDate.plusDays(billingCycleDays);
    }

    public void cancel() {
        if (status == SubscriptionStatus.CANCELED) {
            return; // idempotente
        }

        if (status == SubscriptionStatus.EXPIRED) {
            throw new IllegalStateException("Expired subscription cannot be canceled");
        }

        this.status = SubscriptionStatus.CANCELED;
    }

    public void expireIfNeeded(LocalDate today) {
        if (today.isAfter(endDate) && status != SubscriptionStatus.CANCELED) {
            this.status = SubscriptionStatus.EXPIRED;
        }
    }

}
