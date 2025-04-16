package org.romanzhula.expenses_service.configurations.security;

import lombok.RequiredArgsConstructor;
import org.romanzhula.microservices_common.cors.EnableCORS;
import org.romanzhula.microservices_common.security.jwt.AuthEntryPointJwt;
import org.romanzhula.microservices_common.security.jwt.JwtAuthenticationManager;
import org.romanzhula.microservices_common.security.jwt.JwtServerAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;


@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
@EnableCORS
public class WebSecurityConfiguration {

    private final AuthEntryPointJwt authEntryPointJwtUnauthorizedHandler;


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity httpSecurity,
            JwtAuthenticationManager authenticationManager,
            JwtServerAuthenticationConverter authenticationConverter
    ) {

        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter);

        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPointJwtUnauthorizedHandler)
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/api/v1/auth/**",
                                "/img/**",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/v3/api-docs/**",
                                "/proxy/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build()
        ;
    }

}
