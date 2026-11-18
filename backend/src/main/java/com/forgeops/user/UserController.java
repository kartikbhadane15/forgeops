package com.forgeops.user;

import com.forgeops.common.dto.ApiResponse;
import com.forgeops.common.dto.PagedResponse;
import com.forgeops.user.dto.CreateUserRequest;
import com.forgeops.user.dto.UpdateRolesRequest;
import com.forgeops.user.dto.UpdateUserStatusRequest;
import com.forgeops.user.dto.UserDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Administration", description = "User administration and role management (Admin only)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List users with search and pagination")
    public ResponseEntity<PagedResponse<UserDTO>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int boundedSize = Math.min(Math.max(size, 1), 100);
        Page<UserDTO> usersPage = userService.listUsers(search, PageRequest.of(page, boundedSize, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(PagedResponse.of(usersPage.getContent(), usersPage));
    }

    @PostMapping
    @Operation(summary = "Create a new user account")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(user, "User account created successfully"));
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Update user role assignments")
    public ResponseEntity<ApiResponse<UserDTO>> updateRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolesRequest request) {
        UserDTO user = userService.updateRoles(id, request);
        return ResponseEntity.ok(ApiResponse.ok(user, "User roles updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable user account")
    public ResponseEntity<ApiResponse<UserDTO>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserDTO user = userService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(user, "User status updated successfully"));
    }
}
