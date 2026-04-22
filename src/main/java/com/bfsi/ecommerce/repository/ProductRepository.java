package com.bfsi.ecommerce.repository;

import com.bfsi.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryAndStatus(String category, Product.ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND p.status = 'ACTIVE'")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    List<Product> findByCategoryAndStatusOrderByPriceAsc(String category, Product.ProductStatus status);
}
