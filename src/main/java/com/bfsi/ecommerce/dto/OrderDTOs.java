package com.bfsi.ecommerce.dto;

import com.bfsi.ecommerce.entity.Order;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTOs {

    public static class OrderItemRequest {
        @NotNull private Long productId;
        @NotNull @Min(1) private Integer quantity;

        public Long getProductId()    { return productId; }
        public Integer getQuantity()  { return quantity; }
        public void setProductId(Long productId)   { this.productId = productId; }
        public void setQuantity(Integer quantity)  { this.quantity = quantity; }
    }

    public static class CreateOrderRequest {
        @NotNull @Size(min = 1) private List<OrderItemRequest> items;
        @NotBlank private String shippingAddress;
        @NotNull private Order.PaymentMethod paymentMethod;

        public List<OrderItemRequest> getItems()         { return items; }
        public String getShippingAddress()               { return shippingAddress; }
        public Order.PaymentMethod getPaymentMethod()    { return paymentMethod; }
        public void setItems(List<OrderItemRequest> items){ this.items = items; }
        public void setShippingAddress(String shippingAddress){ this.shippingAddress = shippingAddress; }
        public void setPaymentMethod(Order.PaymentMethod paymentMethod){ this.paymentMethod = paymentMethod; }
    }

    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public Long getProductId()       { return productId; }
        public String getProductName()   { return productName; }
        public Integer getQuantity()     { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal()  { return subtotal; }
        public void setProductId(Long productId)         { this.productId = productId; }
        public void setProductName(String productName)   { this.productName = productName; }
        public void setQuantity(Integer quantity)        { this.quantity = quantity; }
        public void setUnitPrice(BigDecimal unitPrice)   { this.unitPrice = unitPrice; }
        public void setSubtotal(BigDecimal subtotal)     { this.subtotal = subtotal; }
    }

    public static class OrderResponse {
        private Long id;
        private String orderNumber;
        private List<OrderItemResponse> items;
        private BigDecimal totalAmount;
        private Order.OrderStatus status;
        private Order.PaymentMethod paymentMethod;
        private String shippingAddress;
        private LocalDateTime createdAt;

        public Long getId()                          { return id; }
        public String getOrderNumber()               { return orderNumber; }
        public List<OrderItemResponse> getItems()    { return items; }
        public BigDecimal getTotalAmount()           { return totalAmount; }
        public Order.OrderStatus getStatus()         { return status; }
        public Order.PaymentMethod getPaymentMethod(){ return paymentMethod; }
        public String getShippingAddress()           { return shippingAddress; }
        public LocalDateTime getCreatedAt()          { return createdAt; }
        public void setId(Long id)                   { this.id = id; }
        public void setOrderNumber(String orderNumber){ this.orderNumber = orderNumber; }
        public void setItems(List<OrderItemResponse> items){ this.items = items; }
        public void setTotalAmount(BigDecimal totalAmount){ this.totalAmount = totalAmount; }
        public void setStatus(Order.OrderStatus status){ this.status = status; }
        public void setPaymentMethod(Order.PaymentMethod paymentMethod){ this.paymentMethod = paymentMethod; }
        public void setShippingAddress(String shippingAddress){ this.shippingAddress = shippingAddress; }
        public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    }
}
