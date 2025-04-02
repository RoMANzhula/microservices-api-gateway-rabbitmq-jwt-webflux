package org.romanzhula.api_gateway_settings.services;

import lombok.RequiredArgsConstructor;
import org.romanzhula.microservices_common.dto.UserResponse;
import org.romanzhula.microservices_common.utils.UserServiceWebClient;
import org.romanzhula.microservices_common.security.CustomUserDetailsService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserServiceWebClient userServiceWebClient;


    @Override
    public Mono<UserDetails> findByUsername(String username) throws UsernameNotFoundException {
        return userServiceWebClient.getUserByUsername(username)
                .map(userResponse -> User
                        .withUsername(userResponse.getUsername())
                        .password(userResponse.getPassword())
                        .roles(userResponse.getRoles().toArray(new String[0]))
                        .build()
                )
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found with username: " + username)))
        ;
    }

}
