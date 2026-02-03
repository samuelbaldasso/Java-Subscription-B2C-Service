package com.sbaldasso.b2c_subscription_service.infrastructure.repository;

import com.sbaldasso.b2c_subscription_service.domain.model.OutboxEvent;
import com.sbaldasso.b2c_subscription_service.domain.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status ORDER BY e.createdAt ASC")
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'FAILED' AND e.retryCount < 3 AND e.createdAt > :since ORDER BY e.createdAt ASC")
    List<OutboxEvent> findRetryableFailedEvents(LocalDateTime since);

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
