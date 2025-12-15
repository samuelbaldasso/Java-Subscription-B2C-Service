import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Plan management endpoints")
public class PlanController {
    
    private final PlanService planService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Create new plan (Admin only)")
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        PlanResponse plan = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }
    
    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Get plan by ID")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable Long id) {
        PlanResponse plan = planService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }
    
    @GetMapping
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "List all active plans")
    public ResponseEntity<Page<PlanResponse>> listPlans(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<PlanResponse> plans = planService.getActivePlans(pageable);
        return ResponseEntity.ok(plans);
    }
}