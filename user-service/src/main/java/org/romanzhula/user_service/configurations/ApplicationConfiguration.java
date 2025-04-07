package org.romanzhula.user_service.configurations;

import lombok.RequiredArgsConstructor;
import org.romanzhula.microservices_common.cors.CorsAutoConfiguration;
import org.romanzhula.microservices_common.cors.CorsConfigurationProperties;
import org.romanzhula.microservices_common.security.jwt.AuthEntryPointJwt;
import org.romanzhula.microservices_common.security.jwt.CommonJWTService;
import org.romanzhula.microservices_common.security.jwt.JwtAuthenticationManager;
import org.romanzhula.microservices_common.security.jwt.JwtServerAuthenticationConverter;
import org.romanzhula.user_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    @Value("${app.jwt_secret_code}")
    private String secretKey;

    private final UserRepository userRepository;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
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

    @Bean
    public JwtAuthenticationManager authenticationManager() {
        return new JwtAuthenticationManager(commonJWTService());
    }

    @Bean
    public ServerAuthenticationConverter converter() {
        return new JwtServerAuthenticationConverter(commonJWTService());
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(new SimpleGrantedAuthority(user.getRole().name()))
                        .build()
                )
        ;
    }

}

