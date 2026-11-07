package com.viajarpelomundo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsSecurityHttpTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldAllowLocalhostFrontend() throws Exception {
        var response = mvc.perform(
            options("/api/v1/bookings")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(
                    HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                    "POST"
                )
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());
        assertEquals(
            "http://localhost:5173",
            response.getHeader(
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
            )
        );
    }

    @Test
    void shouldAllowLoopbackFrontend() throws Exception {
        var response = mvc.perform(
            options("/api/v1/bookings")
                .header(HttpHeaders.ORIGIN, "http://127.0.0.1:5173")
                .header(
                    HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                    "POST"
                )
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());
        assertEquals(
            "http://127.0.0.1:5173",
            response.getHeader(
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
            )
        );
    }

    @Test
    void shouldRejectUnknownOrigin() throws Exception {
        var response = mvc.perform(
            options("/api/v1/bookings")
                .header(
                    HttpHeaders.ORIGIN,
                    "https://untrusted.example"
                )
                .header(
                    HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                    "POST"
                )
        ).andReturn().getResponse();

        assertEquals(403, response.getStatus());
        assertNull(
            response.getHeader(
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
            )
        );
    }

    @Test
    void shouldProtectBookingDetails() throws Exception {
        var response = mvc.perform(
            get("/api/v1/bookings/test-reference")
        ).andReturn().getResponse();

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldProtectPaymentAction() throws Exception {
        var response = mvc.perform(
            post("/api/v1/bookings/test-reference/pay")
        ).andReturn().getResponse();

        assertEquals(401, response.getStatus());
    }
}
