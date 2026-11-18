package com.forgeops.incident;

import com.forgeops.common.dto.ApiResponse;
import com.forgeops.common.dto.PagedResponse;
import com.forgeops.incident.dto.*;
import com.forgeops.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "Incident lifecycle management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class IncidentController {

    private final IncidentLifecycleService incidentService;

    public IncidentController(IncidentLifecycleService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @Operation(summary = "List incidents with pagination and filtering")
    public ResponseEntity<ApiResponse<PagedResponse<IncidentSummaryDTO>>> listIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<IncidentSummaryDTO> result = incidentService.listIncidents(status, severity, serviceId, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full incident detail by ID")
    public ResponseEntity<ApiResponse<IncidentDTO>> getIncident(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(incidentService.getIncident(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEER', 'APPROVER', 'ADMIN')")
    @Operation(summary = "Create a new incident")
    public ResponseEntity<ApiResponse<IncidentDTO>> createIncident(
            @Valid @RequestBody CreateIncidentRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        IncidentDTO created = incidentService.createIncident(req, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PostMapping("/{id}/transitions")
    @PreAuthorize("hasAnyRole('ENGINEER', 'APPROVER', 'ADMIN')")
    @Operation(summary = "Transition incident status")
    public ResponseEntity<ApiResponse<IncidentDTO>> transitionStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TransitionRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok(incidentService.transitionStatus(id, req)));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ENGINEER', 'APPROVER', 'ADMIN')")
    @Operation(summary = "Assign or unassign an incident")
    public ResponseEntity<ApiResponse<IncidentDTO>> assignIncident(
            @PathVariable UUID id,
            @RequestBody AssignIncidentRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok(incidentService.assignIncident(id, req.assigneeId())));
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('ENGINEER', 'APPROVER', 'ADMIN')")
    @Operation(summary = "Add a progress note to an incident")
    public ResponseEntity<ApiResponse<IncidentNoteDTO>> addNote(
            @PathVariable UUID id,
            @Valid @RequestBody AddNoteRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        IncidentNoteDTO note = incidentService.addNote(id, req, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(note));
    }
}
