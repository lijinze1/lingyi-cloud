package com.lingyi.gateway.server.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.common.core.constant.GlobalConstants;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.domain.Result;
import com.lingyi.gateway.server.util.JwtTokenService;
import com.lingyi.gateway.server.util.JwtTokenService.UserClaims;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.cloud.gateway.server.webflux.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.server.webflux.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = List.of(
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui",
            "/api/user/auth/login",
            "/api/user/auth/register",
            "/api/user/ping"
    );

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

    public AuthenticationGlobalFilter(JwtTokenService jwtTokenService, ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isWhitelisted(path) || !path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(GlobalConstants.AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(GlobalConstants.BEARER_PREFIX)) {
            return writeUnauthorized(exchange.getResponse());
        }

        String token = authorization.substring(GlobalConstants.BEARER_PREFIX.length());
        UserClaims claims = jwtTokenService.parseAndValidate(token);
        if (claims == null) {
            return writeUnauthorized(exchange.getResponse());
        }

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(GlobalConstants.USER_ID_HEADER, String.valueOf(claims.userId()))
                .header(GlobalConstants.USERNAME_HEADER, claims.username())
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitelisted(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> writeUnauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(Result.fail(ErrorCode.UNAUTHORIZED));
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception ex) {
            byte[] bytes = "{\"code\":\"A0401\",\"message\":\"Unauthorized\"}"
                    .getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        }
    }
}
