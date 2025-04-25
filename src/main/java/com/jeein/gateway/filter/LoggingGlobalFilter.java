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
        ServerHttpRequest request = exchange.getRequest();

        String uri = request.getURI().toString();
        log.info("Request Message");

        request.getQueryParams()
                .forEach((param, values) -> log.info("Request Parameter: {} = {}", param, values));
        request.getHeaders()
                .forEach((header, values) -> log.info("Request Header: {} = {}", header, values));

        return chain.filter(exchange)
                .then(
                        Mono.fromRunnable(
                                () -> {
                                    exchange.getResponse()
                                            .beforeCommit(
                                                    () -> {
                                                        var headers =
                                                                exchange.getResponse().getHeaders();

                                                        log.debug("Before header modification:");
                                                        headers.forEach(
                                                                (key, value) ->
                                                                        log.debug(
                                                                                "{}: {}", key,
                                                                                value));

                                                        String allowHeaders =
                                                                headers.getFirst(
                                                                        "Access-Control-Allow-Headers");
                                                        if (allowHeaders != null) {
                                                            headers.set(
                                                                    "Access-Control-Allow-Headers",
                                                                    allowHeaders.split(",")[0]
                                                                            .trim());
                                                        }

                                                        String allowCredentials =
                                                                headers.getFirst(
                                                                        "Access-Control-Allow-Credentials");
                                                        if (allowCredentials != null) {
                                                            headers.set(
                                                                    "Access-Control-Allow-Credentials",
                                                                    allowCredentials.split(",")[0]
                                                                            .trim());
                                                        }

                                                        String allowOrigins =
                                                                headers.getFirst(
                                                                        "Access-Control-Allow-Origin");
                                                        if (allowOrigins != null) {
                                                            headers.set(
                                                                    "Access-Control-Allow-Origin",
                                                                    allowOrigins.split(",")[0]
                                                                            .trim());
                                                        }

                                                        headers.remove("Vary");
                                                        headers.add("Vary", "Origin");
                                                        headers.add(
                                                                "Vary",
                                                                "Access-Control-Request-Method");
                                                        headers.add(
                                                                "Vary",
                                                                "Access-Control-Request-Headers");

                                                        return Mono.empty();
                                                    });

                                    // Post-commit 로그 출력
                                    log.info("After header modification:");
                                    exchange.getResponse()
                                            .getHeaders()
                                            .forEach(
                                                    (key, value) -> log.info("{}: {}", key, value));

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
