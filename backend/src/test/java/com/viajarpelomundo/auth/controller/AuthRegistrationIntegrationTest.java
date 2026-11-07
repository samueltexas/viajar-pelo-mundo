package com.viajarpelomundo.auth.controller;

import com.viajarpelomundo.auth.repository.AppUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRegistrationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AppUserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    void shouldRegisterWithHashedPasswordAndNoPasswordLeak()
        throws Exception {

        String email = "test-" + UUID.randomUUID() + "@example.com";
        String password = "VerySecurePassword123!";

        try {
            mvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email":"%s",
                          "password":"%s"
                        }
                        """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

            var user = users.findByEmail(email).orElseThrow();

            assertNotEquals(password, user.getPasswordHash());
            assertTrue(encoder.matches(password, user.getPasswordHash()));
            assertTrue(user.getPasswordHash().startsWith("$2"));

        } finally {
            users.findByEmail(email).ifPresent(users::delete);
        }
    }
}
