package org.romanzhula.user_service.configurations.security.implementations;

import lombok.RequiredArgsConstructor;
import org.romanzhula.microservices_common.security.CustomUserDetailsService;
import org.romanzhula.microservices_common.security.implementations.UserDetailsImpl;
import org.romanzhula.user_service.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;


    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found with username: " + username)))
                .map(user -> UserDetailsImpl.build(
                        String.valueOf(user.getId()),
                        user.getUsername(),
                        user.getPassword(),
                        List.of(user.getRole().name())
                ))
        ;
    }

}
