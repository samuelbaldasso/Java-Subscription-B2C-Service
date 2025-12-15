package com.sbaldasso.b2c_subscription_service.infrastructure.repository;

import com.sbaldasso.b2c_subscription_service.domain.model.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {
    
    List<PaymentEvent> findByInvoiceId(Long invoiceId);
}