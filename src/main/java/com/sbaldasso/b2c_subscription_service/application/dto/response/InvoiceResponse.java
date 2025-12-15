package com.sbaldasso.b2c_subscription_service.application.dto.response;

import com.sbaldasso.b2c_subscription_service.domain.model.Invoice;
import com.sbaldasso.b2c_subscription_service.domain.model.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    
    private Long id;
    private String invoiceNumber;
    private Long subscriptionId;
    private BigDecimal amount;
    private InvoiceStatus status;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private Boolean isOverdue;
    private LocalDateTime createdAt;
    
    public static InvoiceResponse from(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .subscriptionId(invoice.getSubscription().getId())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .paidAt(invoice.getPaidAt())
                .isOverdue(invoice.isOverdue())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}