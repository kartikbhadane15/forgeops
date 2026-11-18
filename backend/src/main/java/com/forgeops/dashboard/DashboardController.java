package com.forgeops.dashboard;

import com.forgeops.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Dashboard", description = "Operations overview and health summary")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> summary() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getSummary()));
    }

    @GetMapping("/service-health")
    public ResponseEntity<ApiResponse<List<ServiceHealthDTO>>> serviceHealth() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getServiceHealthList()));
    }

    @GetMapping("/recent-incidents")
    public ResponseEntity<ApiResponse<List<com.forgeops.incident.dto.IncidentSummaryDTO>>> recentIncidents() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getRecentIncidents()));
    }
}
