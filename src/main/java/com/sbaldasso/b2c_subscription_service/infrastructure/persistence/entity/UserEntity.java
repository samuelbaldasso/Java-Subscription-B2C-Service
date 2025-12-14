package com.sbaldasso.b2c_subscription_service.infrastructure.persistence.entity;

import com.sbaldasso.b2c_subscription_service.domain.model.Email;
import com.sbaldasso.b2c_subscription_service.domain.model.UserId;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@EqualsAndHashCode
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public UserEntity(Long id, String username, String email, String passwordHash) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
