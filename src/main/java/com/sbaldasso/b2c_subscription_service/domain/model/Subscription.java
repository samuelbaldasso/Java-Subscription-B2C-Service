package com.sbaldasso.b2c_subscription_service.domain.model;

import com.sbaldasso.b2c_subscription_service.domain.exception.SubscriptionException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscription_user", columnList = "user_id"),
    @Index(name = "idx_subscription_status", columnList = "status"),
    @Index(name = "idx_subscription_next_billing", columnList = "nextBillingDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;
    
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    @Column
    private LocalDateTime trialEndDate;
    
    @Column
    private LocalDateTime nextBillingDate;
    
    @Column
    private LocalDateTime canceledAt;
    
    @Column
    private LocalDateTime expiresAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL;
    }
    
    public boolean isInTrial() {
        return status == SubscriptionStatus.TRIAL && 
               trialEndDate != null && 
               LocalDateTime.now().isBefore(trialEndDate);
    }
    
    public void activate() {
        if (status == SubscriptionStatus.CANCELED || status == SubscriptionStatus.EXPIRED) {
            throw new SubscriptionException("Cannot activate a canceled or expired subscription");
        }
        this.status = SubscriptionStatus.ACTIVE;
        calculateNextBillingDate();
    }
    
    public void cancel() {
        if (status == SubscriptionStatus.CANCELED) {
            throw new SubscriptionException("Subscription is already canceled");
        }
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
        this.expiresAt = nextBillingDate != null ? nextBillingDate : LocalDateTime.now();
    }
    
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        this.expiresAt = LocalDateTime.now();
    }
    
    public void renew() {
        if (!isActive()) {
            throw new SubscriptionException("Cannot renew inactive subscription");
        }
        calculateNextBillingDate();
    }
    
    private void calculateNextBillingDate() {
        if (plan.isFree()) {
            this.nextBillingDate = null;
            return;
        }
        
        LocalDateTime baseDate = nextBillingDate != null ? nextBillingDate : LocalDateTime.now();
        this.nextBillingDate = baseDate.plusDays(plan.getBillingCycleInDays());
    }
}