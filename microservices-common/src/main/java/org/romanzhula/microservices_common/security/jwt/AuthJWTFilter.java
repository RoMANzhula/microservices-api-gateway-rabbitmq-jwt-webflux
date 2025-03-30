package org.romanzhula.microservices_common.security.jwt;

import lombok.RequiredArgsConstructor;
import org.romanzhula.microservices_common.security.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthJWTFilter implements WebFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_KEYWORD_IN_HEADER = "Bearer ";

    private final CommonJWTService jwtService;
    private final CustomUserDetailsService userDetailsService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_KEYWORD_IN_HEADER)) {
            return chain.filter(exchange);
        }

        String jwtToken = authHeader.substring(BEARER_KEYWORD_IN_HEADER.length());

        return jwtService.extractUsernameFromToken(jwtToken)
                .flatMap(username -> userDetailsService.findByUsername(username))
                .flatMap(userDetails -> jwtService.isTokenValid(jwtToken, userDetails.getUsername())
                        .flatMap(isValid -> {
                            if (isValid) {
                                return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities()
                                        )
                                ));
                            }
                            return chain.filter(exchange);
                        })
                )
                .onErrorResume(e -> {
                    return chain.filter(exchange);
                })
        ;
    }

}
