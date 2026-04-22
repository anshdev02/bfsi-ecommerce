package com.bfsi.ecommerce.service.impl;

import com.bfsi.ecommerce.entity.*;
import com.bfsi.ecommerce.exception.InsufficientFundsException;
import com.bfsi.ecommerce.exception.ResourceNotFoundException;
import com.bfsi.ecommerce.repository.TransactionRepository;
import com.bfsi.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Object> getWallet(Long userId) {
        User user = getUser(userId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("accountNumber", user.getAccountNumber());
        res.put("balance", user.getWalletBalance());
        res.put("username", user.getUsername());
        res.put("status", user.getStatus());
        return res;
    }

    @Transactional
    public Map<String, Object> topUp(Long userId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (amount.compareTo(BigDecimal.valueOf(1000000)) > 0) {
            throw new IllegalArgumentException("Max top-up per transaction is ₹10,00,000");
        }
        User user = getUser(userId);
        BigDecimal balanceBefore = user.getWalletBalance();
        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        Transaction txn = saveTransaction(user, amount,
                Transaction.TransactionType.CREDIT,
                Transaction.TransactionStatus.SUCCESS,
                balanceBefore, user.getWalletBalance(),
                description != null ? description : "Wallet top-up");

        log.info("Wallet top-up: user={}, amount={}", userId, amount);
        return buildTxnResponse(txn, user.getWalletBalance());
    }

    @Transactional
    public Map<String, Object> transfer(Long fromUserId, String toAccountNumber,
                                        BigDecimal amount, String description) {
        User sender = getUser(fromUserId);
        User receiver = userRepository.findByAccountNumber(toAccountNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + toAccountNumber));

        if (sender.getAccountNumber().equalsIgnoreCase(toAccountNumber.trim())) {
            throw new IllegalArgumentException("Cannot transfer to your own account.");
        }
        if (sender.getWalletBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient balance. Available: ₹" + sender.getWalletBalance());
        }

        BigDecimal senderBefore = sender.getWalletBalance();
        BigDecimal receiverBefore = receiver.getWalletBalance();

        sender.setWalletBalance(sender.getWalletBalance().subtract(amount));
        receiver.setWalletBalance(receiver.getWalletBalance().add(amount));
        userRepository.save(sender);
        userRepository.save(receiver);

        String ref = "TRF" + System.currentTimeMillis();

        saveTransaction(sender, amount, Transaction.TransactionType.TRANSFER,
                Transaction.TransactionStatus.SUCCESS,
                senderBefore, sender.getWalletBalance(),
                "Transfer to " + toAccountNumber + " | " + ref);

        saveTransaction(receiver, amount, Transaction.TransactionType.CREDIT,
                Transaction.TransactionStatus.SUCCESS,
                receiverBefore, receiver.getWalletBalance(),
                "Transfer from " + sender.getAccountNumber() + " | " + ref);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("transactionRef", ref);
        res.put("amountTransferred", amount);
        res.put("newBalance", sender.getWalletBalance());
        res.put("toAccount", toAccountNumber);
        return res;
    }

    public Page<Map<String, Object>> getTransactionHistory(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("ref", t.getTransactionRef());
                    m.put("amount", t.getAmount());
                    m.put("type", t.getType());
                    m.put("status", t.getStatus());
                    m.put("balanceBefore", t.getBalanceBefore());
                    m.put("balanceAfter", t.getBalanceAfter());
                    m.put("description", t.getDescription());
                    m.put("createdAt", t.getCreatedAt());
                    return m;
                });
    }

    private Transaction saveTransaction(User user, BigDecimal amount,
            Transaction.TransactionType type, Transaction.TransactionStatus status,
            BigDecimal before, BigDecimal after, String description) {
        return transactionRepository.save(Transaction.builder()
                .transactionRef("TXN" + System.currentTimeMillis() +
                        (int)(Math.random() * 1000))
                .user(user).amount(amount).type(type).status(status)
                .balanceBefore(before).balanceAfter(after)
                .description(description).build());
    }

    private Map<String, Object> buildTxnResponse(Transaction txn, BigDecimal newBalance) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("transactionRef", txn.getTransactionRef());
        r.put("amount", txn.getAmount());
        r.put("newBalance", newBalance);
        r.put("status", txn.getStatus());
        return r;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
