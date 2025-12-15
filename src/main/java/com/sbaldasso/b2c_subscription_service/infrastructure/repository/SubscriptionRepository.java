package com.sbaldasso.b2c_subscription_service.infrastructure.repository;

import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import com.sbaldasso.b2c_subscription_service.domain.model.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    Optional<Subscription> findByUserId(Long userId);
    
    @Query("SELECT s FROM Subscription s JOIN FETCH s.plan WHERE s.user.id = :userId")
    Optional<Subscription> findByUserIdWithPlan(Long userId);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.nextBillingDate <= :date")
    List<Subscription> findByStatusAndNextBillingDateBefore(SubscriptionStatus status, LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'TRIAL' AND s.trialEndDate <= :date")
    List<Subscription> findExpiredTrials(LocalDateTime date);
    
    Page<Subscription> findByUserId(Long userId, Pageable pageable);
}