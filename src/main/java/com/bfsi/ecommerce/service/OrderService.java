package com.bfsi.ecommerce.service;

import com.bfsi.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface OrderService {
    Map<String, Object> createOrder(Map<String, Object> request, Long userId);
    Map<String, Object> getOrder(Long orderId, Long userId);
    Page<Map<String, Object>> getUserOrders(Long userId, Pageable pageable);
    Map<String, Object> cancelOrder(Long orderId, Long userId);
    Map<String, Object> updateOrderStatus(Long orderId, Order.OrderStatus status); // admin
}
