package com.bfsi.ecommerce.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public OrderItem() {}

    private OrderItem(Builder b) {
        this.order = b.order;
        this.product = b.product;
        this.quantity = b.quantity;
        this.unitPrice = b.unitPrice;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Order order;
        private Product product;
        private Integer quantity;
        private BigDecimal unitPrice;

        public Builder order(Order v)          { this.order = v; return this; }
        public Builder product(Product v)      { this.product = v; return this; }
        public Builder quantity(Integer v)     { this.quantity = v; return this; }
        public Builder unitPrice(BigDecimal v) { this.unitPrice = v; return this; }
        public OrderItem build()               { return new OrderItem(this); }
    }

    public Long getId()              { return id; }
    public Order getOrder()          { return order; }
    public Product getProduct()      { return product; }
    public Integer getQuantity()     { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal()  { return subtotal; }

    public void setId(Long id)               { this.id = id; }
    public void setOrder(Order order)        { this.order = order; }
    public void setProduct(Product product)  { this.product = product; }
    public void setQuantity(Integer quantity){ this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice){ this.unitPrice = unitPrice; }
    public void setSubtotal(BigDecimal subtotal)  { this.subtotal = subtotal; }
}
