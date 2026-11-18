package com.forgeops.auth;

import com.forgeops.auth.dto.AuthResponse;
import com.forgeops.auth.dto.LoginRequest;
import com.forgeops.common.dto.ApiResponse;
import com.forgeops.user.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and session verification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.ok(authResponse, "Authentication successful"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out active session")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Successfully logged out")));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user details")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.ok(user));
    }
}
