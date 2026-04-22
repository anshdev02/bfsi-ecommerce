package com.bfsi.ecommerce.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_ref", unique = true, nullable = false)
    private String transactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "balance_before", precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    private String description;

    @Column(name = "order_id")
    private Long orderId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType { CREDIT, DEBIT, REFUND, TRANSFER }
    public enum TransactionStatus { PENDING, SUCCESS, FAILED, REVERSED }

    public Transaction() {}

    private Transaction(Builder b) {
        this.transactionRef = b.transactionRef;
        this.user = b.user;
        this.amount = b.amount;
        this.type = b.type;
        this.status = b.status;
        this.balanceBefore = b.balanceBefore;
        this.balanceAfter = b.balanceAfter;
        this.description = b.description;
        this.orderId = b.orderId;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String transactionRef, description;
        private User user;
        private BigDecimal amount, balanceBefore, balanceAfter;
        private TransactionType type;
        private TransactionStatus status;
        private Long orderId;

        public Builder transactionRef(String v)    { this.transactionRef = v; return this; }
        public Builder user(User v)                { this.user = v; return this; }
        public Builder amount(BigDecimal v)        { this.amount = v; return this; }
        public Builder type(TransactionType v)     { this.type = v; return this; }
        public Builder status(TransactionStatus v) { this.status = v; return this; }
        public Builder balanceBefore(BigDecimal v) { this.balanceBefore = v; return this; }
        public Builder balanceAfter(BigDecimal v)  { this.balanceAfter = v; return this; }
        public Builder description(String v)       { this.description = v; return this; }
        public Builder orderId(Long v)             { this.orderId = v; return this; }
        public Transaction build()                 { return new Transaction(this); }
    }

    public Long getId()                  { return id; }
    public String getTransactionRef()    { return transactionRef; }
    public User getUser()                { return user; }
    public BigDecimal getAmount()        { return amount; }
    public TransactionType getType()     { return type; }
    public TransactionStatus getStatus() { return status; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public BigDecimal getBalanceAfter()  { return balanceAfter; }
    public String getDescription()       { return description; }
    public Long getOrderId()             { return orderId; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    public void setStatus(TransactionStatus status) { this.status = status; }
}
