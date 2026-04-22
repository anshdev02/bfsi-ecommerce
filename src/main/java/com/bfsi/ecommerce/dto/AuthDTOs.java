package com.bfsi.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public class AuthDTOs {

    public static class LoginRequest {
        @NotBlank
        private String username;

        @NotBlank
        @Size(min = 6, max = 100)
        private String password;

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class SignupRequest {
        @NotBlank
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 6, max = 100)
        private String password;

        @NotBlank
        private String fullName;

        private Set<String> roles;

        public String getUsername() { return username; }
        public String getEmail()    { return email; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public Set<String> getRoles() { return roles; }
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email)       { this.email = email; }
        public void setPassword(String password) { this.password = password; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public void setRoles(Set<String> roles)  { this.roles = roles; }
    }

    public static class JwtResponse {
        private String token;
        private String refreshToken;
        private String type = "Bearer";
        private Long id;
        private String username;
        private String email;
        private List<String> roles;

        public JwtResponse(String token, String refreshToken, Long id,
                           String username, String email, List<String> roles) {
            this.token = token;
            this.refreshToken = refreshToken;
            this.id = id;
            this.username = username;
            this.email = email;
            this.roles = roles;
        }

        public String getToken()        { return token; }
        public String getAccessToken()  { return token; }
        public String getRefreshToken() { return refreshToken; }
        public String getType()         { return type; }
        public Long getId()             { return id; }
        public String getUsername()     { return username; }
        public String getEmail()        { return email; }
        public List<String> getRoles()  { return roles; }
    }

    public static class RefreshTokenRequest {
        @NotBlank
        private String refreshToken;

        public String getRefreshToken()              { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class TokenRefreshResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";

        public TokenRefreshResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken()  { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getTokenType()    { return tokenType; }
    }
}
