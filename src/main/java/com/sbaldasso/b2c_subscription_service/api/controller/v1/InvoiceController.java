package com.sbaldasso.b2c_subscription_service.api.controller.v1;

import com.sbaldasso.b2c_subscription_service.application.dto.response.InvoiceResponse;
import com.sbaldasso.b2c_subscription_service.application.service.InvoiceService;
import com.sbaldasso.b2c_subscription_service.infrastructure.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @GetMapping("/me")
    @Operation(summary = "Get current user invoices")
    public ResponseEntity<Page<InvoiceResponse>> getMyInvoices(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<InvoiceResponse> invoices = invoiceService.getUserInvoices(
                currentUser.getId(), 
                pageable
        );
        return ResponseEntity.ok(invoices);
    }
    
    @GetMapping("/{invoiceNumber}")
    @Operation(summary = "Get invoice by number")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable String invoiceNumber) {
        InvoiceResponse invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(invoice);
    }
}