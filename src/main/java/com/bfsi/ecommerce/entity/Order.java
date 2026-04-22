package com.bfsi.ecommerce.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "transaction_id")
    private String transactionId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, PAYMENT_PROCESSING, PAYMENT_SUCCESS, PAYMENT_FAILED,
        CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }

    public enum PaymentMethod { WALLET, CARD, NET_BANKING, UPI }

    public Order() {}

    private Order(Builder b) {
        this.orderNumber = b.orderNumber;
        this.user = b.user;
        this.items = b.items != null ? b.items : new ArrayList<>();
        this.totalAmount = b.totalAmount;
        this.status = b.status != null ? b.status : OrderStatus.PENDING;
        this.paymentMethod = b.paymentMethod;
        this.shippingAddress = b.shippingAddress;
        this.transactionId = b.transactionId;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String orderNumber, shippingAddress, transactionId;
        private User user;
        private List<OrderItem> items;
        private BigDecimal totalAmount;
        private OrderStatus status;
        private PaymentMethod paymentMethod;

        public Builder orderNumber(String v)       { this.orderNumber = v; return this; }
        public Builder user(User v)                { this.user = v; return this; }
        public Builder items(List<OrderItem> v)    { this.items = v; return this; }
        public Builder totalAmount(BigDecimal v)   { this.totalAmount = v; return this; }
        public Builder status(OrderStatus v)       { this.status = v; return this; }
        public Builder paymentMethod(PaymentMethod v){ this.paymentMethod = v; return this; }
        public Builder shippingAddress(String v)   { this.shippingAddress = v; return this; }
        public Builder transactionId(String v)     { this.transactionId = v; return this; }
        public Order build()                       { return new Order(this); }
    }

    public Long getId()                  { return id; }
    public String getOrderNumber()       { return orderNumber; }
    public User getUser()                { return user; }
    public List<OrderItem> getItems()    { return items; }
    public BigDecimal getTotalAmount()   { return totalAmount; }
    public OrderStatus getStatus()       { return status; }
    public PaymentMethod getPaymentMethod(){ return paymentMethod; }
    public String getShippingAddress()   { return shippingAddress; }
    public String getTransactionId()     { return transactionId; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    public void setId(Long id)                       { this.id = id; }
    public void setOrderNumber(String orderNumber)   { this.orderNumber = orderNumber; }
    public void setUser(User user)                   { this.user = user; }
    public void setItems(List<OrderItem> items)      { this.items = items; }
    public void setTotalAmount(BigDecimal totalAmount){ this.totalAmount = totalAmount; }
    public void setStatus(OrderStatus status)        { this.status = status; }
    public void setPaymentMethod(PaymentMethod paymentMethod){ this.paymentMethod = paymentMethod; }
    public void setShippingAddress(String shippingAddress){ this.shippingAddress = shippingAddress; }
    public void setTransactionId(String transactionId){ this.transactionId = transactionId; }
}
