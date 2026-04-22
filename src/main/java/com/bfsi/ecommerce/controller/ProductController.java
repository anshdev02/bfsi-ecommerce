package com.bfsi.ecommerce.controller;

import com.bfsi.ecommerce.dto.ApiResponse;
import com.bfsi.ecommerce.dto.ProductDTOs.*;
import com.bfsi.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Products", description = "Product catalogue management")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ── Public Endpoints ──────────────────────────────────────────────────

    @GetMapping("/api/products/public")
    @Operation(summary = "List all active products (public)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Page<ProductResponse> products = productService.getAllProducts(
                PageRequest.of(page, size, Sort.by(sortBy).descending()));
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/api/products/public/{id}")
    @Operation(summary = "Get product by ID (public)")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProduct(id)));
    }

    @GetMapping("/api/products/public/search")
    @Operation(summary = "Search products by keyword (public)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(keyword, PageRequest.of(page, size))));
    }

    @GetMapping("/api/products/public/category/{category}")
    @Operation(summary = "Get products by category (public)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> byCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProductsByCategory(category, PageRequest.of(page, size))));
    }

    // ── Admin Endpoints ───────────────────────────────────────────────────

    @PostMapping("/api/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.createProduct(request)));
    }

    @PutMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate product (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated", null));
    }
}
