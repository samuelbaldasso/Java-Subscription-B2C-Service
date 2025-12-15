package com.sbaldasso.b2c_subscription_service.application.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePlanRequest {
    
    @NotBlank(message = "Plan code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Plan code must contain only uppercase letters, numbers, and underscores")
    private String code;
    
    @NotBlank(message = "Plan name is required")
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Billing cycle is required")
    @Min(value = 1, message = "Billing cycle must be at least 1 day")
    private Integer billingCycleInDays = 30;
    
    @Min(value = 0, message = "Trial period cannot be negative")
    private Integer trialPeriodInDays = 7;
}