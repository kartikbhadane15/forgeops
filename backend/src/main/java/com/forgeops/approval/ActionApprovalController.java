package com.forgeops.approval;

import com.forgeops.approval.dto.ActionRequestDTO;
import com.forgeops.approval.dto.CreateActionRequest;
import com.forgeops.approval.dto.ReviewActionRequest;
import com.forgeops.common.dto.ApiResponse;
import com.forgeops.common.dto.PagedResponse;
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
@RequestMapping("/api/v1/action-requests")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Action Requests", description = "Human approval workflow and safe simulated execution")
public class ActionApprovalController {

    private final ApprovalWorkflowService approvalService;

    public ActionApprovalController(ApprovalWorkflowService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    @Operation(summary = "List action requests with filters and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ActionRequestDTO>>> listRequests(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) UUID incidentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<ActionRequestDTO> result = approvalService.listRequests(status, incidentId, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(result.getContent(), result)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get action request detail by ID")
    public ResponseEntity<ApiResponse<ActionRequestDTO>> getRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(approvalService.getRequest(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEER', 'APPROVER', 'ADMIN')")
    @Operation(summary = "Submit a proposed action request for human review")
    public ResponseEntity<ApiResponse<ActionRequestDTO>> createRequest(
            @Valid @RequestBody CreateActionRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ActionRequestDTO created = approvalService.createRequest(req, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created, "Action request submitted for approval"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    @Operation(summary = "Approve an action request (enforces segregation of duties)")
    public ResponseEntity<ApiResponse<ActionRequestDTO>> approveRequest(
            @PathVariable UUID id,
            @RequestBody(required = false) ReviewActionRequest review,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReviewActionRequest r = review != null ? review : new ReviewActionRequest("Approved by operations reviewer");
        return ResponseEntity.ok(ApiResponse.ok(approvalService.approveRequest(id, r, currentUser.getId()), "Action request approved"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    @Operation(summary = "Reject an action request with mandatory justification comment")
    public ResponseEntity<ApiResponse<ActionRequestDTO>> rejectRequest(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewActionRequest review,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok(approvalService.rejectRequest(id, review, currentUser.getId()), "Action request rejected"));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('ENGINEER', 'APPROVER', 'ADMIN')")
    @Operation(summary = "Execute an approved action against simulated target infrastructure")
    public ResponseEntity<ApiResponse<ActionRequestDTO>> executeRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok(approvalService.executeRequest(id, currentUser.getId()), "Action execution processed"));
    }
}
