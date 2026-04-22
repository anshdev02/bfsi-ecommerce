package com.bfsi.ecommerce.service;

import com.bfsi.ecommerce.dto.ProductDTOs.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    ProductResponse getProduct(Long id);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> getProductsByCategory(String category, Pageable pageable);
    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);
}
