package com.jeein.gateway.filter;

import com.jeein.gateway.exception.CustomJwtException;
import com.jeein.gateway.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final PublicKey publicKey;

    public JwtAuthenticationFilter(@Value("${token.jwt.public}") String publicKeyPem) {
        super(Config.class);
        log.debug("public key pem: {}", publicKeyPem);

        try {
            if (publicKeyPem == null || publicKeyPem.isBlank()) {
                throw new IllegalArgumentException("Public key is missing in configuration.");
            }

            this.publicKey = getPublicKeyFromString(publicKeyPem);
        } catch (Exception e) {
            log.error("Failed to initialize JwtTokenManager: {}", e.getMessage(), e);
            throw new CustomJwtException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public static PublicKey getPublicKeyFromString(String publicKeyPem) throws Exception {
        // PEM 형식에서 Base64 부분만 추출
        String publicKeyPEM =
                publicKeyPem
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", ""); // 공백 제거

        // Base64 디코딩
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);

        // X509EncodedKeySpec을 사용하여 PublicKey 객체 생성
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(keySpec);
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        log.info("JwtAuthenticationFilter 실행됨!");

        return (exchange, chain) -> {
            String token = extractToken(exchange.getRequest());

            if (token == null) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(publicKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String memberId = claims.getSubject();
                log.info("JWT 검증 성공: memberId={}", memberId);

                // 헤더에 사용자 정보 추가
                ServerWebExchange modifiedExchange = setHeaders(exchange, memberId);
                return chain.filter(modifiedExchange);
            } catch (Exception e) {
                log.error("JWT 검증 실패: {}", e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private ServerWebExchange setHeaders(ServerWebExchange exchange, String memberId) {
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("x-api-userid", memberId)
                .build();

        return exchange.mutate().request(modifiedRequest).build();
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
