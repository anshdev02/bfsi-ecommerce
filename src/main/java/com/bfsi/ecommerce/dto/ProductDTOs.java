package com.bfsi.ecommerce.dto;

import com.bfsi.ecommerce.entity.Product;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDTOs {

    public static class ProductRequest {
        @NotBlank private String name;
        private String description;
        @NotNull @DecimalMin("0.01") private BigDecimal price;
        @NotNull @Min(0) private Integer stockQuantity;
        @NotBlank private String category;
        private String imageUrl;

        public String getName()           { return name; }
        public String getDescription()    { return description; }
        public BigDecimal getPrice()      { return price; }
        public Integer getStockQuantity() { return stockQuantity; }
        public String getCategory()       { return category; }
        public String getImageUrl()       { return imageUrl; }

        public void setName(String name)                   { this.name = name; }
        public void setDescription(String description)     { this.description = description; }
        public void setPrice(BigDecimal price)             { this.price = price; }
        public void setStockQuantity(Integer stockQuantity){ this.stockQuantity = stockQuantity; }
        public void setCategory(String category)           { this.category = category; }
        public void setImageUrl(String imageUrl)           { this.imageUrl = imageUrl; }
    }

    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private String category;
        private String imageUrl;
        private Product.ProductStatus status;
        private LocalDateTime createdAt;

        public static ProductResponse from(Product p) {
            ProductResponse r = new ProductResponse();
            r.id = p.getId();
            r.name = p.getName();
            r.description = p.getDescription();
            r.price = p.getPrice();
            r.stockQuantity = p.getStockQuantity();
            r.category = p.getCategory();
            r.imageUrl = p.getImageUrl();
            r.status = p.getStatus();
            r.createdAt = p.getCreatedAt();
            return r;
        }

        public Long getId()                  { return id; }
        public String getName()              { return name; }
        public String getDescription()       { return description; }
        public BigDecimal getPrice()         { return price; }
        public Integer getStockQuantity()    { return stockQuantity; }
        public String getCategory()          { return category; }
        public String getImageUrl()          { return imageUrl; }
        public Product.ProductStatus getStatus() { return status; }
        public LocalDateTime getCreatedAt()  { return createdAt; }
    }
}
