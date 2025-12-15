package com.sbaldasso.b2c_subscription_service.api.controller.v1;

import com.sbaldasso.b2c_subscription_service.application.dto.request.CreateUserRequest;
import com.sbaldasso.b2c_subscription_service.application.dto.request.LoginRequest;
import com.sbaldasso.b2c_subscription_service.application.dto.response.JwtResponse;
import com.sbaldasso.b2c_subscription_service.application.dto.response.UserResponse;
import com.sbaldasso.b2c_subscription_service.application.service.UserService;
import com.sbaldasso.b2c_subscription_service.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        String token = jwtTokenProvider.generateToken(authentication);
        UserResponse user = userService.getUserById(
                userService.findByEmail(request.getEmail()).getId()
        );
        
        return ResponseEntity.ok(
                JwtResponse.builder()
                        .token(token)
                        .user(user)
                        .build()
        );
    }
}