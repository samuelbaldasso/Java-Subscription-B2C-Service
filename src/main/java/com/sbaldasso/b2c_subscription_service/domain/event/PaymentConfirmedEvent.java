package com.sbaldasso.b2c_subscription_service.domain.event;

import com.sbaldasso.b2c_subscription_service.domain.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentConfirmedEvent {
    private Invoice invoice;
}