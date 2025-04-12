package org.romanzhula.wallet_service.configurations;

import org.romanzhula.microservices_common.cors.CorsAutoConfiguration;
import org.romanzhula.microservices_common.cors.CorsConfigurationProperties;
import org.romanzhula.microservices_common.security.jwt.AuthEntryPointJwt;
import org.romanzhula.microservices_common.security.jwt.CommonJWTService;
import org.romanzhula.microservices_common.security.jwt.JwtAuthenticationManager;
import org.romanzhula.microservices_common.security.jwt.JwtServerAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ApplicationConfiguration {

    @Value("${app.jwt_secret_code}")
    private String secretKey;

    
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

}
