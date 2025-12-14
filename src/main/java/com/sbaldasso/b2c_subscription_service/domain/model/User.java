package com.sbaldasso.b2c_subscription_service.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class User {
    private final UserId id;
    private final String username;
    private final Email email;
    private final String passwordHash;

    public User(UserId id, String username, Email email, String passwordHash){
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

}
