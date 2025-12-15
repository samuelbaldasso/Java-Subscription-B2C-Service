package com.sbaldasso.b2c_subscription_service.application.dto.response;

import com.sbaldasso.b2c_subscription_service.domain.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    
    private Long id;
    private String email;
    private String name;
    private Boolean active;
    private LocalDateTime createdAt;
    
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}