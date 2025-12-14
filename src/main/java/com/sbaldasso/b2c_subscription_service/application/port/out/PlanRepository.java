package com.sbaldasso.b2c_subscription_service.application.port.out;

import com.sbaldasso.b2c_subscription_service.domain.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

}
