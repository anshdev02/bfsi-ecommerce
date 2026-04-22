package com.bfsi.ecommerce.controller;

import com.bfsi.ecommerce.dto.AuthDTOs.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Order Controller Integration Tests")
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static String userToken;
    private static String adminToken;
    private static Long createdOrderId;

    @BeforeEach
    void setup() throws Exception {
        if (userToken  == null) userToken  = login("user1",  "User@123");
        if (adminToken == null) adminToken = login("admin", "Admin@123");
    }

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username); req.setPassword(password);
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test @Order(1)
    @DisplayName("POST /api/orders — wallet payment order created successfully")
    void createOrder_walletPayment() throws Exception {
        // Get first product id from public listing
        MvcResult productResult = mockMvc.perform(get("/api/products/public"))
                .andReturn();
        Long productId = objectMapper.readTree(productResult.getResponse().getContentAsString())
                .path("data").path("content").get(0).path("id").asLong();

        // Top up wallet first
        mockMvc.perform(post("/api/wallet/topup")
                .header("Authorization", "Bearer " + userToken)
                .param("amount", "5000.00")).andReturn();

        // Place order
        Map<String, Object> orderReq = Map.of(
            "items", List.of(Map.of("productId", productId, "quantity", 1)),
            "shippingAddress", "123 Test Street, Hyderabad, Telangana 500001",
            "paymentMethod", "WALLET"
        );

        MvcResult result = mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andReturn();

        createdOrderId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test @Order(2)
    @DisplayName("GET /api/orders/{id} — user can fetch own order")
    void getOrder_success() throws Exception {
        mockMvc.perform(get("/api/orders/" + createdOrderId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdOrderId))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test @Order(3)
    @DisplayName("GET /api/orders/my-orders — returns paginated order history")
    void getMyOrders() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test @Order(4)
    @DisplayName("POST /api/orders — fails when wallet balance insufficient")
    void createOrder_insufficientBalance() throws Exception {
        MvcResult productResult = mockMvc.perform(get("/api/products/public")).andReturn();
        Long productId = objectMapper.readTree(productResult.getResponse().getContentAsString())
                .path("data").path("content").get(0).path("id").asLong();

        Map<String, Object> orderReq = Map.of(
            "items", List.of(Map.of("productId", productId, "quantity", 1000)),
            "shippingAddress", "Test Address",
            "paymentMethod", "WALLET"
        );

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(
                        status == 402 || status == 400,
                        "Expected 402 or 400 but got " + status);
                });
    }

    @Test @Order(5)
    @DisplayName("PATCH /api/orders/{id}/cancel — user can cancel confirmed order")
    void cancelOrder_success() throws Exception {
        mockMvc.perform(patch("/api/orders/" + createdOrderId + "/cancel")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test @Order(6)
    @DisplayName("PATCH /api/orders/admin/{id}/status — admin can update order status")
    void updateOrderStatus_asAdmin() throws Exception {
        // First create a new order via NET_BANKING (stays PAYMENT_PROCESSING)
        MvcResult productResult = mockMvc.perform(get("/api/products/public")).andReturn();
        Long productId = objectMapper.readTree(productResult.getResponse().getContentAsString())
                .path("data").path("content").get(0).path("id").asLong();

        Map<String, Object> orderReq = Map.of(
            "items", List.of(Map.of("productId", productId, "quantity", 1)),
            "shippingAddress", "Admin Test Address",
            "paymentMethod", "NET_BANKING"
        );

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long newOrderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(patch("/api/orders/admin/" + newOrderId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }
}
