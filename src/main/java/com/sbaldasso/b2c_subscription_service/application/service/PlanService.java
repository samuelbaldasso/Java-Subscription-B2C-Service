package com.sbaldasso.b2c_subscription_service.application.service;

import com.sbaldasso.b2c_subscription_service.application.dto.request.CreatePlanRequest;
import com.sbaldasso.b2c_subscription_service.application.dto.response.PlanResponse;

import com.sbaldasso.b2c_subscription_service.domain.model.Plan;
import com.sbaldasso.b2c_subscription_service.infrastructure.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {
    
    private final PlanRepository planRepository;
    
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        log.info("Creating plan with code: {}", request.getCode());
        
        if (planRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Plan code already exists");
        }
        
        Plan plan = Plan.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .billingCycleInDays(request.getBillingCycleInDays())
                .trialPeriodInDays(request.getTrialPeriodInDays())
                .active(true)
                .build();
        
        Plan savedPlan = planRepository.save(plan);
        log.info("Plan created successfully with id: {}", savedPlan.getId());
        
        return PlanResponse.from(savedPlan);
    }
    
    @Transactional(readOnly = true)
    public PlanResponse getPlanById(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        return PlanResponse.from(plan);
    }
    
    @Transactional(readOnly = true)
    public Plan findByCode(String code) {
        return planRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
    }
    
    @Transactional(readOnly = true)
    public Page<PlanResponse> getActivePlans(Pageable pageable) {
        return planRepository.findByActiveTrue(pageable)
                .map(PlanResponse::from);
    }
}