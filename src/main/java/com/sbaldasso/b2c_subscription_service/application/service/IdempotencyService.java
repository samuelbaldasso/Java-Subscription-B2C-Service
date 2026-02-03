package com.sbaldasso.b2c_subscription_service.application.service;

import com.sbaldasso.b2c_subscription_service.domain.model.ProcessedEvent;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public boolean isAlreadyProcessed(String eventId) {
        return processedEventRepository.existsByEventId(eventId);
    }

    @Transactional
    public void markAsProcessed(String eventId, String eventType) {
        if (!isAlreadyProcessed(eventId)) {
            ProcessedEvent processedEvent = ProcessedEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .build();

            processedEventRepository.save(processedEvent);
            log.debug("Marked event as processed: {}", eventId);
        }
    }
}
