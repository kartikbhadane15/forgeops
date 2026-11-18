package com.forgeops.audit;

import com.forgeops.audit.dto.AuditEventDTO;
import com.forgeops.common.dto.ApiResponse;
import com.forgeops.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-events")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Audit Trail", description = "Immutable security and governance event log")
public class AuditController {

    private final AuditTrailService auditService;

    public AuditController(AuditTrailService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    @Operation(summary = "List immutable audit events (Approver & Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditEventDTO>>> listEvents(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<AuditEventDTO> result = auditService.listEvents(eventType, entityType, entityId, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(result.getContent(), result)));
    }
}
