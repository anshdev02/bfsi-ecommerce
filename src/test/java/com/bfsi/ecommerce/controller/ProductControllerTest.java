package com.bfsi.ecommerce.controller;

import com.bfsi.ecommerce.dto.AuthDTOs.LoginRequest;
import com.bfsi.ecommerce.dto.ProductDTOs.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Product Controller Integration Tests")
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static String adminToken;
    private static String userToken;
    private static Long createdProductId;

    // ── Token setup ────────────────────────────────────────────────────────

    @BeforeEach
    void fetchTokens() throws Exception {
        if (adminToken == null) adminToken = login("admin",  "Admin@123");
        if (userToken  == null) userToken  = login("user1",  "User@123");
    }

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    // ── Public Product Listing ─────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("GET /api/products/public — returns paginated product list")
    void listProducts_public() throws Exception {
        mockMvc.perform(get("/api/products/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/products/public/search — keyword search works")
    void searchProducts() throws Exception {
        mockMvc.perform(get("/api/products/public/search")
                .param("keyword", "card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/products/public/category/{cat} — category filter works")
    void productsByCategory() throws Exception {
        mockMvc.perform(get("/api/products/public/category/Insurance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ── Admin CRUD ─────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("POST /api/admin/products — admin can create product")
    void createProduct_asAdmin() throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName("Test FD Product");
        req.setDescription("Fixed Deposit advisory pack");
        req.setPrice(new BigDecimal("999.00"));
        req.setStockQuantity(100);
        req.setCategory("Banking Services");

        MvcResult result = mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("Test FD Product"))
                .andReturn();

        createdProductId = objectMapper.readTree(
                result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/admin/products — regular user gets 403")
    void createProduct_asUser_forbidden() throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName("Hacked Product");
        req.setDescription("Should not be created");
        req.setPrice(new BigDecimal("1.00"));
        req.setStockQuantity(1);
        req.setCategory("Test");

        mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/products/public/{id} — get product by ID")
    void getProductById() throws Exception {
        mockMvc.perform(get("/api/products/public/" + createdProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdProductId));
    }

    @Test
    @Order(7)
    @DisplayName("PUT /api/admin/products/{id} — admin can update product")
    void updateProduct_asAdmin() throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName("Updated FD Product");
        req.setDescription("Updated description");
        req.setPrice(new BigDecimal("1099.00"));
        req.setStockQuantity(90);
        req.setCategory("Banking Services");

        mockMvc.perform(put("/api/admin/products/" + createdProductId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated FD Product"))
                .andExpect(jsonPath("$.data.price").value(1099.00));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/products/public/999 — 404 for missing product")
    void getProduct_notFound() throws Exception {
        mockMvc.perform(get("/api/products/public/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /api/admin/products/{id} — soft delete works")
    void deleteProduct_asAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/products/" + createdProductId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
