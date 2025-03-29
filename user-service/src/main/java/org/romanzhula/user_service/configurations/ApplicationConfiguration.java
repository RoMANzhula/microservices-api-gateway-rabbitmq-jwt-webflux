package org.romanzhula.user_service.configurations;

import lombok.RequiredArgsConstructor;
import org.romanzhula.microservices_common.cors.CorsAutoConfiguration;
import org.romanzhula.microservices_common.cors.CorsConfigurationProperties;
import org.romanzhula.microservices_common.security.CustomUserDetailsService;
import org.romanzhula.microservices_common.security.jwt.AuthEntryPointJwt;
import org.romanzhula.microservices_common.security.jwt.AuthJWTFilter;
import org.romanzhula.microservices_common.security.jwt.CommonJWTService;
import org.romanzhula.user_service.configurations.security.implementations.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    @Value("${app.jwt_secret_code}")
    private String secretKey;

    private final UserDetailsServiceImpl userDetailServiceImpl;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);

        return manager;
    }

    @Bean
    public AuthJWTFilter authJWTFilter(CommonJWTService jwtService, CustomUserDetailsService userDetailsService) {
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
