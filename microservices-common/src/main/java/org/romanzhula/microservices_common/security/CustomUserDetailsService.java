package org.romanzhula.microservices_common.security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;

public interface CustomUserDetailsService extends ReactiveUserDetailsService {

    Mono<UserDetails> findByUsername(String username) throws UsernameNotFoundException;

}
