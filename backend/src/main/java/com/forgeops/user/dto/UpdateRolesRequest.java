package com.forgeops.user.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class UpdateRolesRequest {

    @NotEmpty(message = "At least one role is required.")
    private Set<String> roles;

    public UpdateRolesRequest() {}

    public UpdateRolesRequest(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
