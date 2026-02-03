package com.sbaldasso.b2c_subscription_service.infrastructure.repository;

import com.sbaldasso.b2c_subscription_service.domain.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    boolean existsByEventId(String eventId);
}
