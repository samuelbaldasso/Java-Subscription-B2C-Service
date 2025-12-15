package com.sbaldasso.b2c_subscription_service.domain.event;

import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionExpiredEvent {
    private Subscription subscription;
}