package com.bfsi.ecommerce.controller;

import com.bfsi.ecommerce.dto.AuthDTOs.LoginRequest;
import com.bfsi.ecommerce.dto.AuthDTOs.SignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Auth Controller Integration Tests")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── Registration ──────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register — success")
    void register_success() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("testuser");
        req.setEmail("testuser@bfsi.com");
        req.setPassword("Test@123");
        req.setFullName("Test User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("registered")));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register — duplicate username returns 400")
    void register_duplicateUsername() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("testuser");          // already created in order-1
        req.setEmail("other@bfsi.com");
        req.setPassword("Test@123");
        req.setFullName("Another User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register — invalid email returns 400")
    void register_invalidEmail() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("anotheruser");
        req.setEmail("not-an-email");
        req.setPassword("Test@123");
        req.setFullName("Bad Email");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/login — success returns JWT")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("Admin@123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/login — wrong password returns 401")
    void login_wrongPassword() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/auth/login — missing fields returns 400")
    void login_missingFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
