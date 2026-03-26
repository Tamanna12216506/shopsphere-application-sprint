package com.capgemini.gatewayserver.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @PostConstruct
    public void init() {
        System.out.println("🔥 JwtAuthenticationFilter bean created");
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        System.out.println("Incoming Request Path: " + path);


        // Public endpoints
        if (isPublicPath(path)) {
            System.out.println("Public endpoint, skipping auth");
            return chain.filter(exchange);
        }
        // ── STEP 2: Check Authorization header exists ────────────────────────
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        System.out.println("Token: " + token);


        if (!jwtUtil.validateToken(token)) {
            System.out.println("❌ Token validation failed");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        // Role-based route checks
        if (path.contains("/api/admin") && !"ADMIN".equals(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        if (path.startsWith("/api/customer")
                && !("CUSTOMER".equals(role) || "ADMIN".equals(role))) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // Forward user info to downstream services
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        headers.set("X-Authenticated-User", username);
        headers.set("X-User-Role", role);

        ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };

        return chain.filter(exchange.mutate().request(decoratedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/signup")
                || path.startsWith("/api/catalog/home")
                || path.startsWith("/api/catalog/products")
                || path.startsWith("/api/catalog/featured")
                || path.startsWith("/api/catalog/categories")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
