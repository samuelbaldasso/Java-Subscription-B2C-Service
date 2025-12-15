package com.sbaldasso.b2c_subscription_service.infrastructure.repository;

import com.sbaldasso.b2c_subscription_service.domain.model.Invoice;
import com.sbaldasso.b2c_subscription_service.domain.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    Page<Invoice> findBySubscriptionId(Long subscriptionId, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.subscription.user.id = :userId ORDER BY i.createdAt DESC")
    Page<Invoice> findByUserId(Long userId, Pageable pageable);
    
    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDateTime date);
}