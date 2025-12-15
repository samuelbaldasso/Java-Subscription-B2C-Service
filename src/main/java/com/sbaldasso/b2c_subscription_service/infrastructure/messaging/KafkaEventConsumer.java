package com.sbaldasso.b2c_subscription_service.infrastructure.messaging;

import com.sbaldasso.b2c_subscription_service.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {
    
    private final PaymentService paymentService;
    
    @KafkaListener(
            topics = "invoice-events",
            groupId = "subscription-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInvoiceEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received invoice event: {}", message);
            
            // Processar evento de invoice
            // Exemplo: disparar pagamento automático
            
            acknowledgment.acknowledge();
            log.info("Invoice event processed successfully");
        } catch (Exception e) {
            log.error("Error processing invoice event", e);
            // Evento será reprocessado ou enviado para DLQ
        }
    }
    
    @KafkaListener(
            topics = "payment-events",
            groupId = "subscription-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received payment event: {}", message);
            
            // Processar evento de pagamento
            // Exemplo: renovar assinatura após pagamento confirmado
            
            acknowledgment.acknowledge();
            log.info("Payment event processed successfully");
        } catch (Exception e) {
            log.error("Error processing payment event", e);
        }
    }
}