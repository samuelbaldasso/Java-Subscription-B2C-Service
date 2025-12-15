package com.sbaldasso.b2c_subscription_service.api.controller.v1;

import com.sbaldasso.b2c_subscription_service.application.dto.request.CreateSubscriptionRequest;
import com.sbaldasso.b2c_subscription_service.application.dto.response.SubscriptionResponse;
import com.sbaldasso.b2c_subscription_service.application.service.SubscriptionService;
import com.sbaldasso.b2c_subscription_service.infrastructure.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    @PostMapping
    @Operation(summary = "Create new subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateSubscriptionRequest request
    ) {
        SubscriptionResponse subscription = subscriptionService.createSubscription(
                currentUser.getId(), 
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        SubscriptionResponse subscription = subscriptionService.getSubscriptionByUserId(
                currentUser.getId()
        );
        return ResponseEntity.ok(subscription);
    }
    
    @DeleteMapping("/me")
    @Operation(summary = "Cancel current user subscription")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        SubscriptionResponse subscription = subscriptionService.cancelSubscription(
                currentUser.getId()
        );
        return ResponseEntity.ok(subscription);
    }
    
    @GetMapping("/me/history")
    @Operation(summary = "Get subscription history")
    public ResponseEntity<Page<SubscriptionResponse>> getSubscriptionHistory(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<SubscriptionResponse> history = subscriptionService.getUserSubscriptionHistory(
                currentUser.getId(), 
                pageable
        );
        return ResponseEntity.ok(history);
    }
}