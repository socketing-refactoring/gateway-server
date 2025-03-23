package com.jeein.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@Order(0)
public class LoggingGlobalFilter implements GlobalFilter {

    @Autowired private RouteLocator routeLocator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Pre Filter Logic
        ServerHttpRequest request = exchange.getRequest();

        String uri = request.getURI().toString();
        log.info("Request URI: {}", uri);

        request.getQueryParams()
                .forEach((param, values) -> log.info("Request Parameter: {} = {}", param, values));
        request.getHeaders()
                .forEach((header, values) -> log.info("Request Header: {} = {}", header, values));

        return chain.filter(exchange)
                .then(
                        Mono.fromRunnable(
                                () -> {
                                    // Post Filter Logic
                                    log.info(
                                            "Response status code: {}",
                                            exchange.getResponse().getStatusCode());

                                    // 변경 전 헤더 로그
                                    var headers = exchange.getResponse().getHeaders();

                                    log.info("Before header modification:");
                                    headers.forEach((key, value) -> log.info("{}: {}", key, value));


                                    // 중복 제거 (Origin, Credentials)
                                    String allowHeaders = headers.getFirst("Access-Control-Allow-Headers");
                                    if (allowHeaders != null) {
                                        String cleanedAllowHeaders = allowHeaders.replaceAll("[\\[\\]]", "");
                                        String[] allowHeadersArray = cleanedAllowHeaders.split(",\\s*");

                                        headers.put("Access-Control-Allow-Headers", List.of(allowHeadersArray[0]));
                                    }

                                    String allowCredentials = headers.getFirst("Access-Control-Allow-Credentials");
                                    if (allowCredentials != null) {
                                        String cleanedCredentialsHeaders = allowCredentials.replaceAll("[\\[\\]]", "");
                                        String[] allowCredentialsArray = cleanedCredentialsHeaders.split(",\\s*");

                                        headers.put("Access-Control-Allow-Credentials", List.of(allowCredentialsArray[0]));
                                    }

                                    String allowOrigins = headers.getFirst("Access-Control-Allow-Origin");
                                    if (allowOrigins != null) {
                                        String cleanedAllowOrigins = allowOrigins.replaceAll("[\\[\\]]", "");
                                        String[] allowOriginsArray = cleanedAllowOrigins.split(",\\s*");

                                        headers.put("Access-Control-Allow-Origin", List.of(allowOriginsArray[0]));
                                    }

                                    // "Vary" 헤더 제거
                                    boolean removed = exchange.getResponse().getHeaders().keySet().removeIf(headerName -> headerName.equalsIgnoreCase("Vary"));

                                    if (removed) {
                                        log.info("Vary header was removed successfully.");
                                    } else {
                                        log.info("No Vary header found.");
                                    }

                                    // Vary 헤더 추가
                                    exchange.getResponse().getHeaders().add("Vary", "Origin");
                                    exchange.getResponse().getHeaders().add("Vary", "Access-Control-Request-Method");
                                    exchange.getResponse().getHeaders().add("Vary", "Access-Control-Request-Headers");

                                    // 변경 후 헤더 로그
                                    log.info("After header modification:");
                                    exchange.getResponse().getHeaders().forEach((key, value) -> log.info("{}: {}", key, value));

                                    Route route =
                                            exchange.getAttribute(
                                                    ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                                    if (route != null) {
                                        log.info("Matched Route ID: {}", route.getId());
                                        log.info("Matched Route URI: {}", route.getUri());
                                    }
                                }));
    }
}
