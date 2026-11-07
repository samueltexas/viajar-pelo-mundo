package com.viajarpelomundo.booking.controller;

import com.viajarpelomundo.booking.service.BookingService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingValidationHttpTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser
    void shouldRejectMoreThanNinePassengers() throws Exception {
        mvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "flightId": 1,
                      "passengers": 15
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.passengers").exists());
    }

    @Test
    @WithMockUser
    void shouldRejectZeroPassengers() throws Exception {
        mvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "flightId": 1,
                      "passengers": 0
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.passengers").exists());
    }

    @Test
    @WithMockUser
    void shouldRejectMissingFlightId() throws Exception {
        mvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "passengers": 2
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.flightId").exists());
    }

    @Test
    @WithMockUser
    void shouldRejectMalformedJson() throws Exception {
        mvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail")
                .value("Invalid or malformed JSON request body"));
    }
}
