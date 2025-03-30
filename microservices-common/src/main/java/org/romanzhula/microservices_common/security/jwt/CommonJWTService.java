package org.romanzhula.microservices_common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.romanzhula.microservices_common.exceptions.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommonJWTService {

    @Value("${app.jwt_secret_code}")
    private String jwtSecretCode;

    private final String secretKey;

    public CommonJWTService(String secret) {
        this.secretKey = jwtSecretCode;
    }


    public Mono<String> generateToken(UserDetails userDetails) {
        Instant issuedAt = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        Instant expiration = issuedAt.plus(2, ChronoUnit.HOURS);

        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(getSecretKey())
                .compact()
        ;

        return Mono.just(token);
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretCode.getBytes());
    }

    public Mono<String> extractUsernameFromToken(String jwtToken) {
        try {
            return Mono.just(extractClaim(jwtToken, Claims::getSubject));
        } catch (Exception e) {
            return Mono.error(new InvalidTokenException("Failed to extract username from JWT token"));
        }
    }

    public <T> T extractClaim(String jwtToken, Function<Claims, T> function) {
        Claims claims = extractAllClaims(jwtToken);
        return function.apply(claims);
    }

    public Claims extractAllClaims(String jwtToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload()
        ;
    }

    public Mono<Boolean> isTokenValid(String jwtToken, String username) {
        return extractUsernameFromToken(jwtToken)
                .map(usernameFromToken -> usernameFromToken.equals(username) && !isTokenExpired(jwtToken))
                .defaultIfEmpty(false)
        ;
    }

    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
    }

    private Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration);
    }

}
