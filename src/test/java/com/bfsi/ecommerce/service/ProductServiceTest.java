package com.bfsi.ecommerce.service;

import com.bfsi.ecommerce.dto.ProductDTOs.ProductRequest;
import com.bfsi.ecommerce.dto.ProductDTOs.ProductResponse;
import com.bfsi.ecommerce.entity.Product;
import com.bfsi.ecommerce.exception.ResourceNotFoundException;
import com.bfsi.ecommerce.repository.ProductRepository;
import com.bfsi.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @InjectMocks ProductServiceImpl productService;

    private Product sampleProduct;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .name("HDFC Credit Card")
                .description("Premium card")
                .price(new BigDecimal("3500.00"))
                .stockQuantity(100)
                .category("Cards")
                .status(Product.ProductStatus.ACTIVE)
                .build();
        // Simulate JPA-assigned id via reflection
        try {
            java.lang.reflect.Field f = Product.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(sampleProduct, 1L);
        } catch (Exception ignored) {}

        sampleRequest = new ProductRequest();
        sampleRequest.setName("HDFC Credit Card");
        sampleRequest.setDescription("Premium card");
        sampleRequest.setPrice(new BigDecimal("3500.00"));
        sampleRequest.setStockQuantity(100);
        sampleRequest.setCategory("Cards");
    }

    @Test
    @DisplayName("createProduct — saves and returns product response")
    void createProduct_success() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(sampleRequest);

        assertThat(response.getName()).isEqualTo("HDFC Credit Card");
        assertThat(response.getPrice()).isEqualByComparingTo("3500.00");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("getProduct — returns product when found")
    void getProduct_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProduct(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("HDFC Credit Card");
    }

    @Test
    @DisplayName("getProduct — throws ResourceNotFoundException when not found")
    void getProduct_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    @DisplayName("getAllProducts — returns page of active products")
    void getAllProducts_returnsPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable))
                .thenReturn(page);

        Page<ProductResponse> result = productService.getAllProducts(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("HDFC Credit Card");
    }

    @Test
    @DisplayName("updateProduct — sets OUT_OF_STOCK when quantity is 0")
    void updateProduct_setsOutOfStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        sampleRequest.setStockQuantity(0);
        ProductResponse response = productService.updateProduct(1L, sampleRequest);

        assertThat(response.getStatus()).isEqualTo(Product.ProductStatus.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("deleteProduct — soft deletes by setting INACTIVE")
    void deleteProduct_softDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.deleteProduct(1L);

        verify(productRepository).save(argThat(p ->
                p.getStatus() == Product.ProductStatus.INACTIVE));
    }

    @Test
    @DisplayName("searchProducts — delegates to repository with keyword")
    void searchProducts_delegatesToRepo() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(productRepository.searchProducts("credit", pageable))
                .thenReturn(new PageImpl<>(List.of(sampleProduct)));

        Page<ProductResponse> result = productService.searchProducts("credit", pageable);

        assertThat(result).isNotEmpty();
        verify(productRepository).searchProducts("credit", pageable);
    }
}
