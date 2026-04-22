package com.bfsi.ecommerce.config;

import com.bfsi.ecommerce.entity.Product;
import com.bfsi.ecommerce.entity.Role;
import com.bfsi.ecommerce.entity.Role.ERole;
import com.bfsi.ecommerce.entity.User;
import com.bfsi.ecommerce.repository.ProductRepository;
import com.bfsi.ecommerce.repository.RoleRepository;
import com.bfsi.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public ApplicationRunner seedData() {
        return args -> {
            seedRoles();
            seedUsers();
            seedProducts();
            log.info("✅ Database seeding complete.");
        };
    }

    private void seedRoles() {
        for (ERole r : ERole.values()) {
            if (roleRepository.findByName(r).isEmpty()) {
                roleRepository.save(new Role(null, r));
                log.info("  Seeded role: {}", r);
            }
        }
    }

    private void seedUsers() {
        createUserIfAbsent("admin",  "admin@bfsi.com",  "Admin@123",  "Admin User",   ERole.ROLE_ADMIN,  "10000.00");
        createUserIfAbsent("banker", "banker@bfsi.com", "Banker@123", "Banker User",  ERole.ROLE_BANKER, "5000.00");
        createUserIfAbsent("user1",  "user1@bfsi.com",  "User@123",   "Regular User", ERole.ROLE_USER,   "2000.00");
    }

    private void createUserIfAbsent(String username, String email, String rawPassword,
                                    String fullName, ERole role, String balance) {
        if (userRepository.existsByUsername(username)) return;

        Role r = roleRepository.findByName(role).orElseThrow();
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .accountNumber("BFSI" + (100000000L + (long)(Math.random() * 900000000L)))
                .walletBalance(new BigDecimal(balance))
                .roles(Set.of(r))
                .build();
        userRepository.save(user);
        log.info("  Seeded user: {} ({})", username, role);
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        Object[][] products = {
            {"HDFC Credit Card",      "Premium rewards credit card with 5% cashback",        "3500.00",  100, "Financial Products"},
            {"Term Insurance Plan",   "1 Crore life cover at ₹12,000/year",                  "12000.00",  50, "Insurance"},
            {"SIP Starter Kit",       "Mutual fund SIP documentation and advisory pack",     "499.00",   200, "Investments"},
            {"Demat Account KYC",     "Full KYC and demat account opening service",          "299.00",   500, "Banking Services"},
            {"Gold Bond Certificate", "Sovereign Gold Bond — 1 unit",                        "6000.00",   30, "Investments"},
            {"Forex Travel Card",     "Multi-currency prepaid travel card",                  "199.00",   150, "Cards"},
            {"Loan Processing Kit",   "Home loan document preparation and advisory",         "1499.00",   80, "Loans"},
            {"UPI Premium Handle",    "Vanity UPI ID registration with premium support",     "99.00",    300, "Digital Banking"},
        };

        for (Object[] p : products) {
            productRepository.save(Product.builder()
                .name((String) p[0])
                .description((String) p[1])
                .price(new BigDecimal((String) p[2]))
                .stockQuantity((Integer) p[3])
                .category((String) p[4])
                .build());
        }
        log.info("  Seeded {} products.", products.length);
    }
}
