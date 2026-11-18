package com.forgeops.serviceinventory;

import com.forgeops.common.dto.ApiResponse;
import com.forgeops.common.dto.PagedResponse;
import com.forgeops.serviceinventory.dto.EnvironmentDTO;
import com.forgeops.serviceinventory.dto.ServiceDTO;
import com.forgeops.serviceinventory.dto.ServiceRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Services", description = "Service catalog management")
public class ServiceInventoryController {

    private final ServiceInventoryService svc;

    public ServiceInventoryController(ServiceInventoryService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ServiceDTO>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ServiceDTO> result = svc.listServices(search,
                PageRequest.of(page, Math.min(size, 100), Sort.by("name").ascending()));
        return ResponseEntity.ok(PagedResponse.of(result.getContent(), result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getService(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ServiceDTO>> create(@Valid @RequestBody ServiceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(svc.createService(req), "Service registered successfully."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ServiceDTO>> update(@PathVariable UUID id, @Valid @RequestBody ServiceRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(svc.updateService(id, req), "Service updated."));
    }

    @GetMapping("/{id}/environments")
    public ResponseEntity<ApiResponse<List<EnvironmentDTO>>> environments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getEnvironmentsForService(id)));
    }
}
