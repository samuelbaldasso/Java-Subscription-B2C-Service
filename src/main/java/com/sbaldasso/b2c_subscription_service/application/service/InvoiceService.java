package com.sbaldasso.b2c_subscription_service.application.service;

import com.sbaldasso.b2c_subscription_service.application.dto.response.InvoiceResponse;
import com.sbaldasso.b2c_subscription_service.domain.event.InvoiceCreatedEvent;
import com.sbaldasso.b2c_subscription_service.domain.model.Invoice;
import com.sbaldasso.b2c_subscription_service.domain.model.InvoiceStatus;
import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public Invoice createInvoice(Subscription subscription) {
        log.info("Creating invoice for subscription: {}", subscription.getId());
        
        Invoice invoice = Invoice.builder()
                .subscription(subscription)
                .amount(subscription.getPlan().getPrice())
                .status(InvoiceStatus.PENDING)
                .dueDate(subscription.getNextBillingDate())
                .build();
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice created successfully with number: {}", savedInvoice.getInvoiceNumber());
        
        eventPublisher.publishEvent(new InvoiceCreatedEvent(savedInvoice));
        
        return savedInvoice;
    }
    
    @Transactional
    public void markInvoiceAsPaid(String invoiceNumber) {
        log.info("Marking invoice as paid: {}", invoiceNumber);
        
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        
        invoice.markAsPaid();
        invoiceRepository.save(invoice);
        
        log.info("Invoice marked as paid successfully");
    }
    
    @Transactional
    public void markInvoiceAsFailed(String invoiceNumber) {
        log.info("Marking invoice as failed: {}", invoiceNumber);
        
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        
        invoice.markAsFailed();
        invoiceRepository.save(invoice);
        
        log.info("Invoice marked as failed");
    }
    
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return InvoiceResponse.from(invoice);
    }
    
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getUserInvoices(Long userId, Pageable pageable) {
        return invoiceRepository.findByUserId(userId, pageable)
                .map(InvoiceResponse::from);
    }
}