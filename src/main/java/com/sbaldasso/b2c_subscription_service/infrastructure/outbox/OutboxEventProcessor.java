package com.sbaldasso.b2c_subscription_service.infrastructure.outbox;

import com.sbaldasso.b2c_subscription_service.domain.model.OutboxEvent;
import com.sbaldasso.b2c_subscription_service.domain.model.OutboxStatus;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (!pendingEvents.isEmpty()) {
            log.debug("Processing {} pending outbox events", pendingEvents.size());
            pendingEvents.forEach(this::processEvent);
        }

        // Retry failed events (last 1 hour, max 3 retries)
        List<OutboxEvent> failedEvents = outboxEventRepository.findRetryableFailedEvents(
                LocalDateTime.now().minusHours(1));

        if (!failedEvents.isEmpty()) {
            log.debug("Retrying {} failed outbox events", failedEvents.size());
            failedEvents.forEach(this::processEvent);
        }
    }

    @Transactional
    protected void processEvent(OutboxEvent event) {
        try {
            publishToKafka(event.getTopic(), event.getPayload());
            event.markAsPublished();
            outboxEventRepository.save(event);

            log.debug("Published outbox event to Kafka: id={}, type={}", event.getId(), event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish outbox event: id={}, type={}", event.getId(), event.getEventType(), e);
            event.markAsFailed(e.getMessage());
            outboxEventRepository.save(event);
        }
    }

    protected void publishToKafka(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
