package com.sbaldasso.b2c_subscription_service.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private UserResponse user;
}