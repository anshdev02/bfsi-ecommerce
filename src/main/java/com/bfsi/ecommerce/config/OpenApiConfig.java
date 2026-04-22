package com.bfsi.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("BFSI E-Commerce Banking API")
                .version("1.0.0")
                .description("""
                    ## Banking & Financial Services REST API
                    
                    A production-grade API demonstrating:
                    - **JWT Authentication** (access + refresh tokens)
                    - **Role-Based Access Control** (USER / BANKER / ADMIN)
                    - **E-Commerce** — products, orders, stock management
                    - **Banking** — wallet, fund transfers, transaction ledger
                    - **Hibernate ORM** with MySQL / H2
                    - **AWS Free Tier** deployable
                    
                    ### Default Test Credentials (seeded on startup)
                    | Username | Password | Role |
                    |----------|----------|------|
                    | admin | Admin@123 | ADMIN |
                    | banker | Banker@123 | BANKER |
                    | user1 | User@123 | USER |
                    """)
                .contact(new Contact()
                    .name("BFSI Dev Team")
                    .email("dev@bfsi.com"))
                .license(new License().name("MIT")))
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort).description("Local"),
                new Server().url("http://your-ec2-ip:8080").description("AWS EC2")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Paste your JWT token here (without 'Bearer ' prefix)")));
    }
}
