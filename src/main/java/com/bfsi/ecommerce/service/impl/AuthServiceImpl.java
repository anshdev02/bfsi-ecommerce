package com.bfsi.ecommerce.service.impl;

import com.bfsi.ecommerce.dto.AuthDTOs.*;
import com.bfsi.ecommerce.entity.Role;
import com.bfsi.ecommerce.entity.Role.ERole;
import com.bfsi.ecommerce.entity.User;
import com.bfsi.ecommerce.repository.RoleRepository;
import com.bfsi.ecommerce.repository.UserRepository;
import com.bfsi.ecommerce.security.JwtUtils;
import com.bfsi.ecommerce.security.UserDetailsImpl;
import com.bfsi.ecommerce.service.AuthService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder encoder,
                           JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, refreshToken,
                userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), roles);
    }

    @Override
    @Transactional
    public String register(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .accountNumber(generateAccountNumber())
                .build();

        Set<String> strRoles = request.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(getRole(ERole.ROLE_USER));
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin"  -> roles.add(getRole(ERole.ROLE_ADMIN));
                    case "banker" -> roles.add(getRole(ERole.ROLE_BANKER));
                    default       -> roles.add(getRole(ERole.ROLE_USER));
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        return "User registered successfully! Account: " + user.getAccountNumber();
    }

    @Override
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }
        String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
        String newAccessToken = jwtUtils.generateTokenFromUsername(username);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);
        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    private Role getRole(ERole eRole) {
        return roleRepository.findByName(eRole)
                .orElseThrow(() -> new RuntimeException("Role not found: " + eRole));
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "BFSI" + (100000000L + (long)(Math.random() * 900000000L));
        } while (userRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
