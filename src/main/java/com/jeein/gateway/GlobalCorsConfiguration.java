package com.jeein.gateway;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@Slf4j
public class GlobalCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(List.of("*"));
        corsConfig.setAllowedMethods(List.of("*"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        log.info("Allowed Origins: {}", corsConfig.getAllowedOrigins());
        log.info("Allowed Origin Patterns: {}", corsConfig.getAllowedOriginPatterns());
        log.info("Allowed Methods: {}", corsConfig.getAllowedMethods());
        log.info("Allowed Headers: {}", corsConfig.getAllowedHeaders());
        log.info("Allow Credentials: {}", corsConfig.getAllowCredentials());
        return new CorsWebFilter(source);
    }
}
