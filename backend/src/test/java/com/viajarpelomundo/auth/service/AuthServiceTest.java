package com.viajarpelomundo.auth.service;

import com.viajarpelomundo.auth.dto.RegisterRequest;
import com.viajarpelomundo.auth.entity.AppUser;
import com.viajarpelomundo.auth.repository.AppUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do serviço de autenticação.
 * Não precisam de uma base de dados real.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository users;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, passwordEncoder);
    }

    @Test
    void shouldRegisterCustomerWithEncodedPassword() {
        when(users.existsByEmail("test@example.com"))
            .thenReturn(false);

        when(passwordEncoder.encode("StrongPassword2026!"))
            .thenReturn("encoded-password");

        when(users.saveAndFlush(any(AppUser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.register(
            new RegisterRequest(
                "TEST@EXAMPLE.COM",
                "StrongPassword2026!"
            )
        );

        assertEquals("test@example.com", response.email());
        assertEquals("CUSTOMER", response.role());

        verify(passwordEncoder).encode("StrongPassword2026!");
        verify(users).saveAndFlush(any(AppUser.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(users.existsByEmail("test@example.com"))
            .thenReturn(true);

        assertThrows(
            ResponseStatusException.class,
            () -> service.register(
                new RegisterRequest(
                    "test@example.com",
                    "StrongPassword2026!"
                )
            )
        );

        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectShortPassword() {
        assertThrows(
            ResponseStatusException.class,
            () -> service.register(
                new RegisterRequest(
                    "test@example.com",
                    "short"
                )
            )
        );

        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(
            ResponseStatusException.class,
            () -> service.register(
                new RegisterRequest(
                    "invalid-email",
                    "StrongPassword2026!"
                )
            )
        );
    }

    @Test
    void shouldFindAuthenticatedUser() {
        var user = new AppUser(
            "test@example.com",
            "encoded-password"
        );

        when(users.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        var response = service.findCurrentUser(
            "test@example.com"
        );

        assertEquals("test@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
    }
}
