package com.sbaldasso.b2c_subscription_service.application.dto.response;

import com.sbaldasso.b2c_subscription_service.domain.model.Plan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PlanResponse {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer billingCycleInDays;
    private Integer trialPeriodInDays;
    private Boolean active;
    private LocalDateTime createdAt;
    
    public static PlanResponse from(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .billingCycleInDays(plan.getBillingCycleInDays())
                .trialPeriodInDays(plan.getTrialPeriodInDays())
                .active(plan.getActive())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}