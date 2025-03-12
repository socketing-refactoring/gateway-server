//package com.jeein.gateway.filter;
//
//import com.jeein.gateway.dto.JwtMemberDTO;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Component
//public class JwtAuthenticationFilter implements GatewayFilter {
//  private final WebClient webClient;
//
//  public JwtAuthenticationFilter(WebClient.Builder webClientBuilder) {
//    this.webClient = webClientBuilder.baseUrl("http://localhost").build();
//  }
//
//  @Override
//  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//    String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//
//    if (token == null || !token.startsWith("Bearer ")) {
//      return onError(exchange, "Missing or Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
//    }
//
//    String tokenWithoutBearer = token.substring(7);
//
//    return webClient.get()
//        .uri("/api/v1/auth/validate?token=" + tokenWithoutBearer)
//        .retrieve()
//        .onStatus(status -> status.isError(), response -> Mono.error(new RuntimeException("Unauthorized")))
//        .bodyToMono(JwtMemberDTO.class)
//        .flatMap(member -> chain.filter(exchange))
//        .onErrorResume(e -> onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED));
//  }
//
//  private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
//    exchange.getResponse().setStatusCode(status);
//    return exchange.getResponse().setComplete();
//  }
//}
