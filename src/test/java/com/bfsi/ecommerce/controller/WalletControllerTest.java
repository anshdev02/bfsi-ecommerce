package com.bfsi.ecommerce.controller;

import com.bfsi.ecommerce.dto.AuthDTOs.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Wallet / Banking Controller Integration Tests")
class WalletControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static String userToken;
    private static String user2Token;
    private static String user2AccountNumber;

    @BeforeEach
    void setup() throws Exception {
        if (userToken  == null) userToken  = login("user1",  "User@123");
        if (user2Token == null) user2Token = login("banker", "Banker@123");
        if (user2AccountNumber == null) {
            MvcResult r = mockMvc.perform(get("/api/wallet")
                    .header("Authorization", "Bearer " + user2Token))
                    .andReturn();
            user2AccountNumber = objectMapper.readTree(r.getResponse().getContentAsString())
                    .path("data").path("accountNumber").asText();
        }
    }

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username); req.setPassword(password);
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test @Order(1)
    @DisplayName("GET /api/wallet — returns balance and account number")
    void getWallet() throws Exception {
        mockMvc.perform(get("/api/wallet")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").exists())
                .andExpect(jsonPath("$.data.accountNumber").exists());
    }

    @Test @Order(2)
    @DisplayName("GET /api/wallet — unauthenticated returns 401")
    void getWallet_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/wallet"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(3)
    @DisplayName("POST /api/wallet/topup — successfully tops up wallet")
    void topUp_success() throws Exception {
        mockMvc.perform(post("/api/wallet/topup")
                .header("Authorization", "Bearer " + userToken)
                .param("amount", "500.00")
                .param("description", "Test top-up"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.newBalance").exists())
                .andExpect(jsonPath("$.data.transactionRef").isNotEmpty());
    }

    @Test @Order(4)
    @DisplayName("POST /api/wallet/topup — rejects amount exceeding limit")
    void topUp_exceedsLimit() throws Exception {
        mockMvc.perform(post("/api/wallet/topup")
                .header("Authorization", "Bearer " + userToken)
                .param("amount", "9999999.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test @Order(5)
    @DisplayName("POST /api/wallet/transfer — successful fund transfer")
    void transfer_success() throws Exception {
        // Top up sender first
        mockMvc.perform(post("/api/wallet/topup")
                .header("Authorization", "Bearer " + userToken)
                .param("amount", "1000.00"))
                .andExpect(status().isOk());

        // Now transfer
        mockMvc.perform(post("/api/wallet/transfer")
                .header("Authorization", "Bearer " + userToken)
                .param("toAccountNumber", user2AccountNumber)
                .param("amount", "200.00")
                .param("description", "Test transfer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionRef").isNotEmpty())
                .andExpect(jsonPath("$.data.amountTransferred").value(200.00));
    }

    @Test @Order(6)
    @DisplayName("POST /api/wallet/transfer — fails if insufficient balance")
    void transfer_insufficientFunds() throws Exception {
        mockMvc.perform(post("/api/wallet/transfer")
                .header("Authorization", "Bearer " + userToken)
                .param("toAccountNumber", user2AccountNumber)
                .param("amount", "99999999.00"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test @Order(7)
    @DisplayName("GET /api/wallet/transactions — returns transaction history")
    void getTransactionHistory() throws Exception {
        mockMvc.perform(get("/api/wallet/transactions")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
