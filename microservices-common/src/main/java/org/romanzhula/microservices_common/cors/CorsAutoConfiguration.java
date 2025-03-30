package org.romanzhula.microservices_common.cors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class CorsAutoConfiguration {

    @Value("${allowed_cross_origin}")
    private String allowedOrigin;

    private final CorsConfigurationProperties properties;

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        org.springframework.web.cors.CorsConfiguration globalConfiguration = new org.springframework.web.cors.CorsConfiguration();
        globalConfiguration.setAllowedOrigins(getAllowedOrigins());
        globalConfiguration.setAllowedMethods(getAllowedMethods());
        globalConfiguration.setAllowedHeaders(getAllowedHeaders());
        globalConfiguration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", globalConfiguration);

        return new CorsWebFilter(source);
    }

    private List<String> getAllowedOrigins() {
        List<String> allowedOrigins = properties.getAllowedOrigins();

        if (!allowedOrigins.isEmpty()) {
            return allowedOrigins;
        }

        return Collections.singletonList("*"); // "*" - for all; allowedOrigin - for client side(frontend)
    }

    private List<String> getAllowedHeaders() {
        List<String> allowedHeaders = properties.getAllowedHeaders();

        if (!allowedHeaders.isEmpty()) {
            return allowedHeaders;
        }

        return Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.REFERER,
                HttpHeaders.USER_AGENT,
                HttpHeaders.CACHE_CONTROL,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.COOKIE,
                "X-Requested-With",
                "X-Forwarded-For",
                "x-ijt"
        );
    }

    private List<String> getAllowedMethods() {
        List<String> allowedMethods = properties.getAllowedMethods();

        if (!allowedMethods.isEmpty()) {
            return allowedMethods;
        }

        return Arrays.stream(HttpMethod.values())
                .map(HttpMethod::name)
                .collect(Collectors.toList())
        ;
    }

}
