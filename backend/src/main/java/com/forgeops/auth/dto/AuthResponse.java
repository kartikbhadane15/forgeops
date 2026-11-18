package com.forgeops.auth.dto;

import com.forgeops.user.dto.UserDTO;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserDTO user;

    public AuthResponse() {}

    public AuthResponse(String token, long expiresIn, UserDTO user) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UserDTO getUser() {
        return user;
    }
}
