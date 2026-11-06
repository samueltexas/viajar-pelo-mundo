package com.viajarpelomundo.auth.dto;

import com.viajarpelomundo.auth.entity.AppUser;

public record UserResponse(
    Long id,
    String email,
    String role
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getRole()
        );
    }
}
