package com.forgeops.auth;

import com.forgeops.audit.AuditTrailService;
import com.forgeops.audit.AuditEventType;
import com.forgeops.auth.dto.AuthResponse;
import com.forgeops.auth.dto.LoginRequest;
import com.forgeops.common.exception.ResourceNotFoundException;
import com.forgeops.security.JwtTokenProvider;
import com.forgeops.security.UserPrincipal;
import com.forgeops.user.UserEntity;
import com.forgeops.user.UserRepository;
import com.forgeops.user.dto.UserDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AuditTrailService auditTrailService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            UserRepository userRepository,
            AuditTrailService auditTrailService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.auditTrailService = auditTrailService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserEntity user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getId()));

        auditTrailService.record(
                user.getId(),
                AuditEventType.AUTH_LOGIN_SUCCESS,
                "USER",
                user.getId().toString(),
                null,
                String.format("User %s successfully authenticated via JWT", user.getEmail()),
                Map.of("email", user.getEmail())
        );

        return new AuthResponse(jwt, tokenProvider.getExpirationMs(), UserDTO.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResourceNotFoundException("No authenticated user session found.");
        }

        UserEntity user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getId()));

        return UserDTO.fromEntity(user);
    }
}
