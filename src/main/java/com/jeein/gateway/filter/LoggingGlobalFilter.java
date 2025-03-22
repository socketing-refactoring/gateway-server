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
