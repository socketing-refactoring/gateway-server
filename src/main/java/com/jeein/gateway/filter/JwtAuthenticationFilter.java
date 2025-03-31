package com.jeein.gateway.filter;

import com.jeein.gateway.dto.JwtMemberDTO;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final WebClient webClient;

    public JwtAuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder.baseUrl("http://auth-service").build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (token == null || !token.startsWith("Bearer ")) {
                setUnauthorizedHeaders(exchange);
                return chain.filter(exchange);
            }

            String tokenWithoutBearer = token.substring(7);

            return webClient.get()
                    .uri("/api/v1/auth/validate?token=" + tokenWithoutBearer)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Unauthorized")))
                    .bodyToMono(JwtMemberDTO.class)
                    .flatMap(member -> {
                        setHeaders(exchange, member);
                        return chain.filter(exchange);
                    })
                    .onErrorResume(e -> onError(exchange, HttpStatus.UNAUTHORIZED));
        };
    }

    private void setUnauthorizedHeaders(ServerWebExchange exchange) {
        exchange.getRequest().mutate()
                .header("x-api-userid", "null")
                .header("x-api-useremail", "null")
                .header("x-api-username", "null")
                .header("x-api-usernickname", "null");
    }

    private void setHeaders(ServerWebExchange exchange, JwtMemberDTO member) {
        exchange.getRequest().mutate()
                .header("x-api-userid", member.getMemberId())
                .header("x-api-useremail", member.getMemberEmail())
                .header("x-api-username", member.getMemberName())
                .header("x-api-usernickname", member.getMemberNickname());
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}
