package com.sbaldasso.b2c_subscription_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbaldasso.b2c_subscription_service.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String SUBSCRIPTION_TOPIC = "subscription-events";
    private static final String INVOICE_TOPIC = "invoice-events";
    private static final String PAYMENT_TOPIC = "payment-events";
    
    @EventListener
    public void handleSubscriptionCreated(SubscriptionCreatedEvent event) {
        publishEvent(SUBSCRIPTION_TOPIC, "subscription.created", event);
    }
    
    @EventListener
    public void handleSubscriptionCanceled(SubscriptionCanceledEvent event) {
        publishEvent(SUBSCRIPTION_TOPIC, "subscription.canceled", event);
    }
    
    @EventListener
    public void handleSubscriptionExpired(SubscriptionExpiredEvent event) {
        publishEvent(SUBSCRIPTION_TOPIC, "subscription.expired", event);
    }
    
    @EventListener
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        publishEvent(INVOICE_TOPIC, "invoice.created", event);
    }
    
    @EventListener
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        publishEvent(PAYMENT_TOPIC, "payment.confirmed", event);
    }
    
    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        publishEvent(PAYMENT_TOPIC, "payment.failed", event);
    }
    
    private void publishEvent(String topic, String eventType, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, eventType, message);
            log.info("Published event {} to topic {}", eventType, topic);
        } catch (Exception e) {
            log.error("Error publishing event {} to topic {}", eventType, topic, e);
        }
    }
}