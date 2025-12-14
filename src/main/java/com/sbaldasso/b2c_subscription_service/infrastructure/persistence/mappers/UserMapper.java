package com.sbaldasso.b2c_subscription_service.infrastructure.persistence.mappers;

import com.sbaldasso.b2c_subscription_service.domain.model.Email;
import com.sbaldasso.b2c_subscription_service.domain.model.User;
import com.sbaldasso.b2c_subscription_service.domain.model.UserId;
import com.sbaldasso.b2c_subscription_service.infrastructure.persistence.entity.UserEntity;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        return new User(
                new UserId(entity.getId()),
                entity.getUsername(),
                new Email(entity.getEmail()),
                entity.getPasswordHash()
        );
    }

    public static UserEntity toEntity(User domain) {
        return new UserEntity(
                domain.getId().getId(),
                domain.getUsername(),
                domain.getEmail().email(),
                domain.getPasswordHash()
        );
    }
}
