package org.romanzhula.microservices_common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.romanzhula.microservices_common.exceptions.InvalidTokenException;
import org.romanzhula.microservices_common.security.jwt.interfaces.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Service
public class CommonJWTService implements TokenProvider {

    private final String jwtSecretCode;
    private final Instant issuedAt = LocalDateTime.now().toInstant(ZoneOffset.UTC);
    private final Instant expiration = issuedAt.plus(2, ChronoUnit.HOURS);


    public CommonJWTService(@Value("${app.jwt_secret_code}") String jwtSecretCode) {
        this.jwtSecretCode = jwtSecretCode;
    }


    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {

        String token = Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(role -> "ROLE_" + role)
                        .toArray())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(getSecretKey())
                .compact()
        ;

        return token;
    }

    String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    List<String> extractRoles(String jwt) {
        return extractClaim(jwt, claims -> (List<String>) claims.get("roles"));
    }


    boolean isTokenValid(String jwt) {
        return !isTokenExpired(jwt);
    }

    private boolean isTokenExpired(String jwt) {
        return extractClaim(jwt, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String jwt, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(jwt);

        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwt) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload()
            ;
        } catch (JwtException e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretCode.getBytes());
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

}
