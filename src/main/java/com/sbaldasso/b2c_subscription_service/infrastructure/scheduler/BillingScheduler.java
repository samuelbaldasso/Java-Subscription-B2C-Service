package com.sbaldasso.b2c_subscription_service.infrastructure.scheduler;

import com.sbaldasso.b2c_subscription_service.application.service.InvoiceService;
import com.sbaldasso.b2c_subscription_service.application.service.SubscriptionService;
import com.sbaldasso.b2c_subscription_service.domain.model.Subscription;
import com.sbaldasso.b2c_subscription_service.domain.model.SubscriptionStatus;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {
    
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceService invoiceService;
    private final SubscriptionService subscriptionService;
    
    @Scheduled(cron = "0 0 2 * * *") // Executa às 2h da manhã todos os dias
    @Transactional
    public void processDueBillings() {
        log.info("Starting billing process for due subscriptions");
        
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> dueSubscriptions = subscriptionRepository
                .findByStatusAndNextBillingDateBefore(SubscriptionStatus.ACTIVE, now);
        
        log.info("Found {} subscriptions due for billing", dueSubscriptions.size());
        
        for (Subscription subscription : dueSubscriptions) {
            try {
                invoiceService.createInvoice(subscription);
                log.info("Invoice created for subscription {}", subscription.getId());
            } catch (Exception e) {
                log.error("Error creating invoice for subscription {}", subscription.getId(), e);
            }
        }
        
        log.info("Billing process completed");
    }
    
    @Scheduled(cron = "0 30 2 * * *") // Executa às 2:30h da manhã todos os dias
    @Transactional
    public void expireTrialSubscriptions() {
        log.info("Starting trial expiration process");
        
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredTrials = subscriptionRepository.findExpiredTrials(now);
        
        log.info("Found {} expired trials", expiredTrials.size());
        
        for (Subscription subscription : expiredTrials) {
            try {
                subscription.expire();
                subscriptionRepository.save(subscription);
                log.info("Trial expired for subscription {}", subscription.getId());
            } catch (Exception e) {
                log.error("Error expiring trial for subscription {}", subscription.getId(), e);
            }
        }
        
        log.info("Trial expiration process completed");
    }
}