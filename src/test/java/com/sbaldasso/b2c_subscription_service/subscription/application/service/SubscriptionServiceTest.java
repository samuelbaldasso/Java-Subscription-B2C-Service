import com.sbaldasso.b2c_subscription_service.application.service;

import com.subscription.application.dto.request.CreateSubscriptionRequest;
import com.subscription.application.dto.response.SubscriptionResponse;
import com.subscription.domain.event.SubscriptionCreatedEvent;
import com.subscription.domain.exception.SubscriptionException;
import com.subscription.domain.model.*;
import com.subscription.infrastructure.repository.PlanRepository;
import com.subscription.infrastructure.repository.SubscriptionRepository;
import com.subscription.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    
    @Mock
    private SubscriptionRepository subscriptionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PlanRepository planRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private SubscriptionService subscriptionService;
    
    private User user;
    private Plan plan;
    private Subscription subscription;
    
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .active(true)
                .build();
        
        plan = Plan.builder()
                .id(1L)
                .code("PREMIUM")
                .name("Premium Plan")
                .price(new BigDecimal("29.99"))
                .billingCycleInDays(30)
                .trialPeriodInDays(7)
                .active(true)
                .build();
        
        subscription = Subscription.builder()
                .id(1L)
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.TRIAL)
                .build();
    }
    
    @Test
    void shouldCreateSubscriptionSuccessfully() {
        // Given
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanCode("PREMIUM");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findByCode("PREMIUM")).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        
        // When
        SubscriptionResponse response = subscriptionService.createSubscription(1L, request);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(eventPublisher).publishEvent(any(SubscriptionCreatedEvent.class));
    }
    
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanCode("PREMIUM");
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> subscriptionService.createSubscription(1L, request));
    }
    
    @Test
    void shouldThrowExceptionWhenUserHasActiveSubscription() {
        // Given
        user.setActiveSubscription(subscription);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanCode("PREMIUM");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // When & Then
        assertThrows(SubscriptionException.class, 
                () -> subscriptionService.createSubscription(1L, request));
    }
    
    @Test
    void shouldCancelSubscriptionSuccessfully() {
        // Given
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        
        // When
        SubscriptionResponse response = subscriptionService.cancelSubscription(1L);
        
        // Then
        assertNotNull(response);
        verify(subscriptionRepository).save(any(Subscription.class));
    }
}