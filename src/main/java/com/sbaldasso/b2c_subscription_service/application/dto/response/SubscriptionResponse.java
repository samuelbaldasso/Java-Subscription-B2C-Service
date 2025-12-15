package com.sbaldasso.b2c_subscription_service.application.dto.response;

import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import com.sbaldasso.b2c_subscription_service.domain.model.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponse {
    
    private Long id;
    private Long userId;
    private PlanResponse plan;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime trialEndDate;
    private LocalDateTime nextBillingDate;
    private LocalDateTime canceledAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private Boolean isInTrial;
    private LocalDateTime createdAt;
    
    public static SubscriptionResponse from(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser().getId())
                .plan(PlanResponse.from(subscription.getPlan()))
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .trialEndDate(subscription.getTrialEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .canceledAt(subscription.getCanceledAt())
                .expiresAt(subscription.getExpiresAt())
                .isActive(subscription.isActive())
                .isInTrial(subscription.isInTrial())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}