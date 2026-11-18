package com.forgeops.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class CreateUserRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Valid email address is required.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String password;

    @NotBlank(message = "Full name is required.")
    private String fullName;

    @NotEmpty(message = "At least one role is required.")
    private Set<String> roles;

    public CreateUserRequest() {}

    public CreateUserRequest(String email, String password, String fullName, Set<String> roles) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
