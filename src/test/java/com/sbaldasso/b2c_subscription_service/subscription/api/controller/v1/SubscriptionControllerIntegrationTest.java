import com.sbaldasso.b2c_subscription_service.api.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.application.dto.request.CreatePlanRequest;
import com.subscription.application.dto.request.CreateSubscriptionRequest;
import com.subscription.application.dto.request.CreateUserRequest;
import com.subscription.application.dto.request.LoginRequest;
import com.subscription.application.dto.response.JwtResponse;
import com.subscription.domain.model.Role;
import com.subscription.domain.model.SubscriptionStatus;
import com.subscription.domain.model.User;
import com.subscription.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SubscriptionControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private String userToken;
    private String adminToken;
    
    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        
        // Create admin user
        User admin = User.builder()
                .email("admin@example.com")
                .name("Admin User")
                .password(passwordEncoder.encode("password"))
                .role(Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);
        
        // Login admin
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@example.com");
        adminLogin.setPassword("password");
        
        MvcResult adminResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        
        JwtResponse adminResponse = objectMapper.readValue(
                adminResult.getResponse().getContentAsString(), 
                JwtResponse.class
        );
        adminToken = adminResponse.getToken();
        
        // Create regular user
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setEmail("user@example.com");
        userRequest.setName("Test User");
        userRequest.setPassword("password");
        
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());
        
        // Login user
        LoginRequest userLogin = new LoginRequest();
        userLogin.setEmail("user@example.com");
        userLogin.setPassword("password");
        
        MvcResult userResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();
        
        JwtResponse userResponse = objectMapper.readValue(
                userResult.getResponse().getContentAsString(), 
                JwtResponse.class
        );
        userToken = userResponse.getToken();
        
        // Create a plan
        CreatePlanRequest planRequest = new CreatePlanRequest();
        planRequest.setCode("PREMIUM");
        planRequest.setName("Premium Plan");
        planRequest.setDescription("Premium subscription plan");
        planRequest.setPrice(new BigDecimal("29.99"));
        planRequest.setBillingCycleInDays(30);
        planRequest.setTrialPeriodInDays(7);
        
        mockMvc.perform(post("/api/v1/plans")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planRequest)))
                .andExpect(status().isCreated());
    }
    
    @Test
    void shouldCreateSubscriptionSuccessfully() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanCode("PREMIUM");
        
        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.TRIAL.name()))
                .andExpect(jsonPath("$.plan.code").value("PREMIUM"));
    }
    
    @Test
    void shouldGetCurrentSubscription() throws Exception {
        // First create a subscription
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanCode("PREMIUM");
        
        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Then get it
        mockMvc.perform(get("/api/v1/subscriptions/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.plan.code").value("PREMIUM"));
    }
    
    @Test
    void shouldCancelSubscription() throws Exception {
        // First create a subscription
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanCode("PREMIUM");
        
        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Then cancel it
        mockMvc.perform(delete("/api/v1/subscriptions/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(SubscriptionStatus.CANCELED.name()));
    }
    
    @Test
    void shouldReturnUnauthorizedWhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions/me"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void shouldPreventDuplicateSubscription() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanCode("PREMIUM");
        
        // First subscription
        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Try to create another
        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}