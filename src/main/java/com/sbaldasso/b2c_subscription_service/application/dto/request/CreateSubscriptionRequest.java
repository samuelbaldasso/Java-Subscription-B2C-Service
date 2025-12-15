package com.sbaldasso.b2c_subscription_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    
    @NotBlank(message = "Plan code is required")
    private String planCode;
}