package com.viajarpelomundo.auth.dto;

public record RegisterRequest(
    String email,
    String password
) {
}
