package com.sbaldasso.b2c_subscription_service.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbaldasso.b2c_subscription_service.domain.model.OutboxEvent;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publish(String aggregateId, String aggregateType, String eventType, Object event, String topic) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(eventType)
                    .payload(payload)
                    .topic(topic)
                    .build();

            outboxEventRepository.save(outboxEvent);

            log.debug("Saved event to outbox: type={}, aggregateId={}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event to JSON: {}", event, e);
            throw new RuntimeException("Failed to publish event to outbox", e);
        }
    }
}
