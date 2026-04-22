package com.bfsi.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stockQuantity;

    @NotBlank
    private String category;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ProductStatus { ACTIVE, INACTIVE, OUT_OF_STOCK }

    public Product() {}

    private Product(Builder b) {
        this.name = b.name;
        this.description = b.description;
        this.price = b.price;
        this.stockQuantity = b.stockQuantity;
        this.category = b.category;
        this.imageUrl = b.imageUrl;
        this.status = b.status != null ? b.status : ProductStatus.ACTIVE;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name, description, category, imageUrl;
        private BigDecimal price;
        private Integer stockQuantity;
        private ProductStatus status;

        public Builder name(String v)              { this.name = v; return this; }
        public Builder description(String v)       { this.description = v; return this; }
        public Builder price(BigDecimal v)         { this.price = v; return this; }
        public Builder stockQuantity(Integer v)    { this.stockQuantity = v; return this; }
        public Builder category(String v)          { this.category = v; return this; }
        public Builder imageUrl(String v)          { this.imageUrl = v; return this; }
        public Builder status(ProductStatus v)     { this.status = v; return this; }
        public Product build()                     { return new Product(this); }
    }

    public Long getId()                  { return id; }
    public String getName()              { return name; }
    public String getDescription()       { return description; }
    public BigDecimal getPrice()         { return price; }
    public Integer getStockQuantity()    { return stockQuantity; }
    public String getCategory()          { return category; }
    public String getImageUrl()          { return imageUrl; }
    public ProductStatus getStatus()     { return status; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    public void setId(Long id)                       { this.id = id; }
    public void setName(String name)                 { this.name = name; }
    public void setDescription(String description)   { this.description = description; }
    public void setPrice(BigDecimal price)           { this.price = price; }
    public void setStockQuantity(Integer stockQuantity){ this.stockQuantity = stockQuantity; }
    public void setCategory(String category)         { this.category = category; }
    public void setImageUrl(String imageUrl)         { this.imageUrl = imageUrl; }
    public void setStatus(ProductStatus status)      { this.status = status; }
}
