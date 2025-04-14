package org.romanzhula.wallet_service.configurations;

import lombok.RequiredArgsConstructor;
import org.romanzhula.microservices_common.cors.CorsAutoConfiguration;
import org.romanzhula.microservices_common.cors.CorsConfigurationProperties;
import org.romanzhula.microservices_common.security.jwt.AuthEntryPointJwt;
import org.romanzhula.microservices_common.security.jwt.CommonJWTService;
import org.romanzhula.microservices_common.security.jwt.JwtAuthenticationManager;
import org.romanzhula.microservices_common.security.jwt.JwtServerAuthenticationConverter;
import org.romanzhula.microservices_common.utils.UserServiceWebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    @Value("${app.jwt_secret_code}")
    private String secretKey;

    @Value("${user-service.url}")
    private String userServiceUrl;

    
    @Bean
    public AuthEntryPointJwt authEntryPointJwt() {
        return new AuthEntryPointJwt();
    }

    @Bean
    public JwtAuthenticationManager authenticationManager() {
        return new JwtAuthenticationManager(commonJWTService());
    }

    @Bean
    public CommonJWTService commonJWTService() {
        return new CommonJWTService(secretKey);
    }

    @Bean
    public JwtServerAuthenticationConverter jwtServerAuthenticationConverter() {
        return new JwtServerAuthenticationConverter(commonJWTService());
    }

    @Bean
    public CorsAutoConfiguration corsAutoConfiguration() {
        return new CorsAutoConfiguration(properties());
    }

    @Bean
    public CorsConfigurationProperties properties() {
        return new CorsConfigurationProperties();
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService(UserServiceWebClient userServiceWebClient) {
        return username -> userServiceWebClient.getUserByUsername(username)
                .map(userResponse -> User
                        .withUsername(userResponse.getUsername())
                        .password(userResponse.getPassword())
                        .roles(userResponse.getRoles().toArray(new String[0]))
                        .build()
                )
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found with username: " + username)))
        ;
    }

    @Bean
    public UserServiceWebClient userServiceWebClient() {
        return new UserServiceWebClient(userServiceUrl);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

}
