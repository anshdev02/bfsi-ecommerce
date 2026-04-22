package com.bfsi.ecommerce.service.impl;

import com.bfsi.ecommerce.entity.*;
import com.bfsi.ecommerce.exception.InsufficientFundsException;
import com.bfsi.ecommerce.exception.ResourceNotFoundException;
import com.bfsi.ecommerce.repository.*;
import com.bfsi.ecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.PageImpl;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            TransactionRepository transactionRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(Map<String, Object> request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsReq =
                (List<Map<String, Object>>) request.get("items");

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map<String, Object> itemReq : itemsReq) {
            Long productId = Long.valueOf(itemReq.get("productId").toString());
            int qty = Integer.parseInt(itemReq.get("quantity").toString());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

            if (product.getStatus() != Product.ProductStatus.ACTIVE) {
                throw new IllegalArgumentException("Product unavailable: " + product.getName());
            }
            if (product.getStockQuantity() < qty) {
                throw new IllegalArgumentException(
                        "Insufficient stock for: " + product.getName() +
                        ". Available: " + product.getStockQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - qty);
            if (product.getStockQuantity() == 0) {
                product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(qty)
                    .unitPrice(product.getPrice())
                    .build();
            item.calculateSubtotal();
            orderItems.add(item);
            total = total.add(item.getSubtotal());
        }

        Order.PaymentMethod paymentMethod = Order.PaymentMethod.valueOf(
                request.get("paymentMethod").toString());

        String transactionId = null;
        if (paymentMethod == Order.PaymentMethod.WALLET) {
            if (user.getWalletBalance().compareTo(total) < 0) {
                throw new InsufficientFundsException(
                        "Insufficient wallet balance. Required: ₹" + total +
                        ", Available: ₹" + user.getWalletBalance());
            }

            BigDecimal balanceBefore = user.getWalletBalance();
            user.setWalletBalance(user.getWalletBalance().subtract(total));
            userRepository.save(user);

            Transaction txn = Transaction.builder()
                    .transactionRef("TXN" + System.currentTimeMillis())
                    .user(user)
                    .amount(total)
                    .type(Transaction.TransactionType.DEBIT)
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .balanceBefore(balanceBefore)
                    .balanceAfter(user.getWalletBalance())
                    .description("Order payment")
                    .build();
            transactionRepository.save(txn);
            transactionId = txn.getTransactionRef();
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .totalAmount(total)
                .status(paymentMethod == Order.PaymentMethod.WALLET
                        ? Order.OrderStatus.CONFIRMED
                        : Order.OrderStatus.PAYMENT_PROCESSING)
                .paymentMethod(paymentMethod)
                .shippingAddress(request.get("shippingAddress").toString())
                .transactionId(transactionId)
                .build();

        Order savedOrder = orderRepository.save(order);
        orderItems.forEach(item -> item.setOrder(savedOrder));
        savedOrder.setItems(orderItems);
        orderRepository.save(savedOrder);

        log.info("Order {} created for user {}", savedOrder.getOrderNumber(), userId);
        return buildOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getUserOrders(Long userId, Pageable pageable) {
        List<Order> allOrders = orderRepository.findByUserIdWithItems(userId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allOrders.size());
        List<Map<String, Object>> content = allOrders.subList(start, end)
                .stream().map(this::buildOrderResponse).toList();
        return new PageImpl<>(content, pageable, allOrders.size());
    }

    @Override
    @Transactional
    public Map<String, Object> cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == Order.OrderStatus.DELIVERED ||
            order.getStatus() == Order.OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("Cannot cancel order in status: " + order.getStatus());
        }

        if (order.getPaymentMethod() == Order.PaymentMethod.WALLET &&
            order.getStatus() == Order.OrderStatus.CONFIRMED) {
            User user = order.getUser();
            BigDecimal balanceBefore = user.getWalletBalance();
            user.setWalletBalance(user.getWalletBalance().add(order.getTotalAmount()));
            userRepository.save(user);

            Transaction refund = Transaction.builder()
                    .transactionRef("REF" + System.currentTimeMillis())
                    .user(user)
                    .amount(order.getTotalAmount())
                    .type(Transaction.TransactionType.REFUND)
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .balanceBefore(balanceBefore)
                    .balanceAfter(user.getWalletBalance())
                    .description("Refund for cancelled order " + order.getOrderNumber())
                    .orderId(order.getId())
                    .build();
            transactionRepository.save(refund);
        }

        order.getItems().forEach(item -> {
            Product p = item.getProduct();
            p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
            p.setStatus(Product.ProductStatus.ACTIVE);
            productRepository.save(p);
        });

        order.setStatus(Order.OrderStatus.CANCELLED);
        return buildOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public Map<String, Object> updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setStatus(status);
        return buildOrderResponse(orderRepository.save(order));
    }

    private Map<String, Object> buildOrderResponse(Order order) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", order.getId());
        res.put("orderNumber", order.getOrderNumber());
        res.put("status", order.getStatus());
        res.put("paymentMethod", order.getPaymentMethod());
        res.put("totalAmount", order.getTotalAmount());
        res.put("shippingAddress", order.getShippingAddress());
        res.put("transactionId", order.getTransactionId());
        res.put("createdAt", order.getCreatedAt());

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> i = new LinkedHashMap<>();
            i.put("productId", item.getProduct().getId());
            i.put("productName", item.getProduct().getName());
            i.put("quantity", item.getQuantity());
            i.put("unitPrice", item.getUnitPrice());
            i.put("subtotal", item.getSubtotal());
            items.add(i);
        }
        res.put("items", items);
        return res;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
