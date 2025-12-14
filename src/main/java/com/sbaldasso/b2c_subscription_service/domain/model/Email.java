package com.sbaldasso.b2c_subscription_service.domain.model;

public record Email(String email) {
    public Email {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}
