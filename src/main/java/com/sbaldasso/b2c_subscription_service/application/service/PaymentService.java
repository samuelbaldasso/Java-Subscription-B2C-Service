package com.sbaldasso.b2c_subscription_service.application.service;

import com.sbaldasso.b2c_subscription_service.domain.event.PaymentConfirmedEvent;
import com.sbaldasso.b2c_subscription_service.domain.event.PaymentFailedEvent;
import com.sbaldasso.b2c_subscription_service.domain.model.Invoice;
import com.sbaldasso.b2c_subscription_service.domain.model.PaymentEvent;
import com.sbaldasso.b2c_subscription_service.domain.model.PaymentStatus;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.InvoiceRepository;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.PaymentEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentEventRepository paymentEventRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public void processPayment(String invoiceNumber) {
        log.info("Processing payment for invoice: {}", invoiceNumber);
        
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .invoice(invoice)
                .amount(invoice.getAmount())
                .status(PaymentStatus.PROCESSING)
                .build();
        
        paymentEventRepository.save(paymentEvent);
        
        // Simula processamento de pagamento
        boolean paymentSuccess = simulatePaymentGateway();
        
        if (paymentSuccess) {
            paymentEvent.setStatus(PaymentStatus.SUCCESS);
            paymentEvent.setGatewayResponse("Payment processed successfully");
            paymentEventRepository.save(paymentEvent);
            
            invoiceService.markInvoiceAsPaid(invoiceNumber);
            
            log.info("Payment processed successfully for invoice: {}", invoiceNumber);
            eventPublisher.publishEvent(new PaymentConfirmedEvent(invoice));
        } else {
            paymentEvent.setStatus(PaymentStatus.FAILED);
            paymentEvent.setGatewayResponse("Payment failed - insufficient funds");
            paymentEventRepository.save(paymentEvent);
            
            invoiceService.markInvoiceAsFailed(invoiceNumber);
            
            log.warn("Payment failed for invoice: {}", invoiceNumber);
            eventPublisher.publishEvent(new PaymentFailedEvent(invoice));
        }
    }
    
    private boolean simulatePaymentGateway() {
        // Simula 90% de sucesso
        return Math.random() > 0.1;
    }
}