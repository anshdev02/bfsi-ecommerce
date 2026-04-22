package com.bfsi.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "username")
})
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "wallet_balance", precision = 15, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UserStatus { ACTIVE, SUSPENDED, CLOSED }

    public User() {}

    private User(Builder b) {
        this.username = b.username;
        this.email = b.email;
        this.password = b.password;
        this.fullName = b.fullName;
        this.accountNumber = b.accountNumber;
        this.walletBalance = b.walletBalance != null ? b.walletBalance : BigDecimal.ZERO;
        this.status = b.status != null ? b.status : UserStatus.ACTIVE;
        this.roles = b.roles != null ? b.roles : new HashSet<>();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username, email, password, fullName, accountNumber;
        private BigDecimal walletBalance;
        private UserStatus status;
        private Set<Role> roles;

        public Builder username(String v)        { this.username = v; return this; }
        public Builder email(String v)           { this.email = v; return this; }
        public Builder password(String v)        { this.password = v; return this; }
        public Builder fullName(String v)        { this.fullName = v; return this; }
        public Builder accountNumber(String v)   { this.accountNumber = v; return this; }
        public Builder walletBalance(BigDecimal v){ this.walletBalance = v; return this; }
        public Builder status(UserStatus v)      { this.status = v; return this; }
        public Builder roles(Set<Role> v)        { this.roles = v; return this; }
        public User build()                      { return new User(this); }
    }

    public Long getId()                  { return id; }
    public String getUsername()          { return username; }
    public String getEmail()             { return email; }
    public String getPassword()          { return password; }
    public String getFullName()          { return fullName; }
    public String getAccountNumber()     { return accountNumber; }
    public BigDecimal getWalletBalance() { return walletBalance; }
    public UserStatus getStatus()        { return status; }
    public Set<Role> getRoles()          { return roles; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    public void setId(Long id)                          { this.id = id; }
    public void setUsername(String username)            { this.username = username; }
    public void setEmail(String email)                  { this.email = email; }
    public void setPassword(String password)            { this.password = password; }
    public void setFullName(String fullName)            { this.fullName = fullName; }
    public void setAccountNumber(String accountNumber)  { this.accountNumber = accountNumber; }
    public void setWalletBalance(BigDecimal walletBalance){ this.walletBalance = walletBalance; }
    public void setStatus(UserStatus status)            { this.status = status; }
    public void setRoles(Set<Role> roles)               { this.roles = roles; }
}
