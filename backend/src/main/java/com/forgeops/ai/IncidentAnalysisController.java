package com.forgeops.ai;

import com.forgeops.ai.dto.AnalysisResultDTO;
import com.forgeops.common.dto.ApiResponse;
import com.forgeops.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents/{incidentId}")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "AI Advisory", description = "Deterministic local incident analysis engine")
public class IncidentAnalysisController {

    private final RuleBasedIncidentAnalysisService analysisService;

    public IncidentAnalysisController(RuleBasedIncidentAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/ai-analyses")
    @Operation(summary = "Get historical analyses for an incident")
    public ResponseEntity<ApiResponse<List<AnalysisResultDTO>>> getAnalyses(@PathVariable UUID incidentId) {
        return ResponseEntity.ok(ApiResponse.ok(analysisService.getAnalysesForIncident(incidentId)));
    }

    @PostMapping("/ai-analysis")
    @PreAuthorize("hasAnyRole('ENGINEER', 'APPROVER', 'ADMIN')")
    @Operation(summary = "Run deterministic local operational analysis for an incident")
    public ResponseEntity<ApiResponse<AnalysisResultDTO>> runAnalysis(
            @PathVariable UUID incidentId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        AnalysisResultDTO result = analysisService.runAnalysis(incidentId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "Operational analysis generated successfully"));
    }
}
