package com.sbaldasso.b2c_subscription_service.domain.model;

import com.sbaldasso.b2c_subscription_service.domain.valueobject.SubscriptionStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Subscription {

    private Long id;
    private Long userId;
    private Long planId;

    private SubscriptionStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDate nextBillingDate; // Importante para o ciclo de cobrança

    private boolean trial;

    private Subscription(Long userId, Plan plan) {
        this.userId = userId;
        this.planId = plan.getId();
        this.startDate = LocalDate.now();

        if (plan.hasTrial()) {
            this.trial = true;
            this.status = SubscriptionStatus.TRIAL;
            this.endDate = startDate.plusDays(plan.getTrialDays());
            this.nextBillingDate = this.endDate;
        } else {
            this.trial = false;
            this.status = SubscriptionStatus.ACTIVE;
            this.endDate = startDate.plusDays(plan.getBillingCycleDays());
            this.nextBillingDate = this.startDate;
        }
    }

    public static Subscription create(Long userId, Plan plan) {
        return new Subscription(userId, plan);
    }

    public void renew(Plan plan) {
        if (status == SubscriptionStatus.CANCELED) {
            throw new IllegalStateException("Cannot renew canceled subscription");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.trial = false;
        // Estende a data final com base no ciclo do plano
        this.endDate = this.endDate.plusDays(plan.getBillingCycleDays());
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
    }
}