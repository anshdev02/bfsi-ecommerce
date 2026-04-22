package com.bfsi.ecommerce.service;

import com.bfsi.ecommerce.dto.AuthDTOs.*;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    String register(SignupRequest request);
    TokenRefreshResponse refreshToken(RefreshTokenRequest request);
}
