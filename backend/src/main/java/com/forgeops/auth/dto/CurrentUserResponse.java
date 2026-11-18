package com.forgeops.auth.dto;

import com.forgeops.user.dto.UserDTO;

public class CurrentUserResponse {

    private UserDTO user;

    public CurrentUserResponse() {}

    public CurrentUserResponse(UserDTO user) {
        this.user = user;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
