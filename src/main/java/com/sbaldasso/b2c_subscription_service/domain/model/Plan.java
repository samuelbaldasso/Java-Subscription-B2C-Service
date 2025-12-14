package com.sbaldasso.b2c_subscription_service.domain.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Plan {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer billingCycleDays;
    private Integer trialDays;
    private boolean active;

    public Plan(Long id, String name, String description, BigDecimal price, Integer billingCycleDays, Integer trialDays, boolean active) {
        if (billingCycleDays == null || billingCycleDays <= 0) {
            throw new IllegalArgumentException("Billing cycle must be positive");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.billingCycleDays = billingCycleDays;
        this.trialDays = (trialDays != null) ? trialDays : 0;
        this.active = active;
    }

    public boolean hasTrial() {
        return this.trialDays > 0;
    }
}