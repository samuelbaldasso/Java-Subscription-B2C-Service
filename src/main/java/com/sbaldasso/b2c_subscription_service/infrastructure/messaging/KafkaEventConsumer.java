package com.sbaldasso.b2c_subscription_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbaldasso.b2c_subscription_service.application.service.IdempotencyService;
import com.sbaldasso.b2c_subscription_service.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "invoice-events", groupId = "subscription-service", containerFactory = "kafkaListenerContainerFactory")
    public void handleInvoiceEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received invoice event: {}", message);

            // Extract event ID (simplified - in real scenario would parse JSON)
            String eventId = extractEventId(message);

            if (idempotencyService.isAlreadyProcessed(eventId)) {
                log.info("Duplicate invoice event detected, skipping: {}", eventId);
                acknowledgment.acknowledge();
                return;
            }

            // Process invoice event
            // Example: trigger automatic payment

            idempotencyService.markAsProcessed(eventId, "InvoiceEvent");
            acknowledgment.acknowledge();
            log.info("Invoice event processed successfully: {}", eventId);
        } catch (Exception e) {
            log.error("Error processing invoice event", e);
            // Event will be reprocessed or sent to DLQ
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "subscription-service", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received payment event: {}", message);

            // Extract event ID
            String eventId = extractEventId(message);

            if (idempotencyService.isAlreadyProcessed(eventId)) {
                log.info("Duplicate payment event detected, skipping: {}", eventId);
                acknowledgment.acknowledge();
                return;
            }

            // Process payment event
            // Example: renew subscription after confirmed payment

            idempotencyService.markAsProcessed(eventId, "PaymentEvent");
            acknowledgment.acknowledge();
            log.info("Payment event processed successfully: {}", eventId);
        } catch (Exception e) {
            log.error("Error processing payment event", e);
        }
    }

    private String extractEventId(String message) {
        try {
            // In real scenario, parse JSON and extract event_id field
            // For now, use hash of message as event ID
            var node = objectMapper.readTree(message);
            if (node.has("eventId")) {
                return node.get("eventId").asText();
            }
            if (node.has("id")) {
                return node.get("id").asText();
            }
            return String.valueOf(message.hashCode());
        } catch (Exception e) {
            log.warn("Failed to extract event ID, using message hash", e);
            return String.valueOf(message.hashCode());
        }
    }
}