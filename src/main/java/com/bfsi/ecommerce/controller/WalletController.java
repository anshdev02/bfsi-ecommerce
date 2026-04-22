package com.bfsi.ecommerce.controller;

import com.bfsi.ecommerce.dto.ApiResponse;
import com.bfsi.ecommerce.security.UserDetailsImpl;
import com.bfsi.ecommerce.service.impl.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@Tag(name = "Wallet / Banking", description = "Wallet management and fund transfers")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @Operation(summary = "Get wallet balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWallet(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                walletService.getWallet(userDetails.getId())));
    }

    @PostMapping("/topup")
    @Operation(summary = "Top up wallet")
    public ResponseEntity<ApiResponse<Map<String, Object>>> topUp(
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Wallet topped up",
                walletService.topUp(userDetails.getId(), amount, description)));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds to another account")
    public ResponseEntity<ApiResponse<Map<String, Object>>> transfer(
            @RequestParam String toAccountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Transfer successful",
                walletService.transfer(userDetails.getId(), toAccountNumber, amount, description)));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> transactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                walletService.getTransactionHistory(userDetails.getId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
}
