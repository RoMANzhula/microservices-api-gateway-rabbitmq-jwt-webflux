package org.romanzhula.api_gateway_settings.configurations;

import lombok.RequiredArgsConstructor;
import org.romanzhula.api_gateway_settings.services.CustomUserDetailsServiceImpl;
import org.romanzhula.microservices_common.cors.CorsAutoConfiguration;
import org.romanzhula.microservices_common.cors.CorsConfigurationProperties;
import org.romanzhula.microservices_common.security.jwt.AuthEntryPointJwt;
import org.romanzhula.microservices_common.security.jwt.AuthJWTFilter;
import org.romanzhula.microservices_common.security.jwt.CommonJWTService;
import org.romanzhula.microservices_common.utils.UserServiceWebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    @Value("${app.jwt_secret_code}")
    private String secretKey;

    @Value("${user-service.url}")
    private String userServiceUrl;


    @Bean
    public UserServiceWebClient userServiceWebClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(userServiceUrl + "/api/v1/users")
                .build()
        ;

        return new UserServiceWebClient(webClient.toString());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
            CustomUserDetailsServiceImpl userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
        ;

        manager.setPasswordEncoder(passwordEncoder);

        return manager;
    }

    @Bean
    public AuthJWTFilter authJWTFilter(
            CommonJWTService jwtService,
            CustomUserDetailsServiceImpl userDetailsService
    ) {
        return new AuthJWTFilter(jwtService, userDetailsService);
    }

    @Bean
    public AuthEntryPointJwt authEntryPointJwt() {
        return new AuthEntryPointJwt();
    }

    @Bean
    public CommonJWTService commonJWTService() {
        return new CommonJWTService(secretKey);
    }

    @Bean
    public CorsAutoConfiguration corsAutoConfiguration() {
        return new CorsAutoConfiguration(properties());
    }

    @Bean
    public CorsConfigurationProperties properties() {
        return new CorsConfigurationProperties();
    }

}