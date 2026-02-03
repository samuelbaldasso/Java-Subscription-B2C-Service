package com.sbaldasso.b2c_subscription_service.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events", indexes = {
        @Index(name = "idx_processed_event_id", columnList = "eventId", unique = true),
        @Index(name = "idx_processed_event_type", columnList = "eventType"),
        @Index(name = "idx_processed_at", columnList = "processedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();
}
