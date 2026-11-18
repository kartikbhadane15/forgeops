package com.forgeops.user.dto;

import com.forgeops.user.UserEntity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserDTO {

    private UUID id;
    private String email;
    private String fullName;
    private boolean enabled;
    private Set<String> roles;
    private Instant createdAt;

    public UserDTO() {}

    public UserDTO(UUID id, String email, String fullName, boolean enabled, Set<String> roles, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.enabled = enabled;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public static UserDTO fromEntity(UserEntity user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                roleNames,
                user.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
