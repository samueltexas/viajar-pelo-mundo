package com.viajarpelomundo.auth.service;

import com.viajarpelomundo.auth.dto.RegisterRequest;
import com.viajarpelomundo.auth.dto.UserResponse;
import com.viajarpelomundo.auth.entity.AppUser;
import com.viajarpelomundo.auth.repository.AppUserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Gere o registo dos utilizadores.
 * A password é convertida num hash BCrypt antes de ser guardada.
 */
@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        AppUserRepository users,
        PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (request == null
            || request.email() == null
            || request.password() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Email and password are required"
            );
        }

        String email = request.email()
            .trim()
            .toLowerCase(Locale.ROOT);

        if (email.length() > 254
            || !EMAIL_PATTERN.matcher(email).matches()
            || request.password().length() < 12
            || request.password().length() > 72) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid email or password length (12-72 characters)"
            );
        }

        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email already registered"
            );
        }

        AppUser user = new AppUser(
            email,
            passwordEncoder.encode(request.password())
        );

        try {
            return UserResponse.from(users.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Unable to register this email",
                exception
            );
        }
    }

    @Transactional(readOnly = true)
    public UserResponse findCurrentUser(String email) {
        return users.findByEmail(email)
            .map(UserResponse::from)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User not found"
            ));
    }
}
