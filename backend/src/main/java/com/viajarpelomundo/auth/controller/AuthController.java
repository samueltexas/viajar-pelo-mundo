package com.viajarpelomundo.auth.controller;

import com.viajarpelomundo.auth.dto.RegisterRequest;
import com.viajarpelomundo.auth.dto.UserResponse;
import com.viajarpelomundo.auth.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API de registo e consulta do utilizador autenticado.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return service.findCurrentUser(authentication.getName());
    }
}
