package org.romanzhula.microservices_common.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.romanzhula.microservices_common.exceptions.InvalidTokenException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final CommonJWTService jwtService;


    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!(authentication instanceof JWToken jwtToken)) {
            return Mono.error(new InvalidTokenException("Invalid authentication type."));
        }

        String token = jwtToken.getToken();

        if (!jwtService.isTokenValid(token)) {
            return Mono.error(new InvalidTokenException("Invalid token."));
        }

        String username = jwtService.extractUsername(token);

        List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(token).stream()
                .map(SimpleGrantedAuthority::new)
                .toList()
        ;

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("")
                .authorities(authorities)
                .build()
        ;

        Authentication authenticated =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        return Mono.just(authenticated);
    }

}
