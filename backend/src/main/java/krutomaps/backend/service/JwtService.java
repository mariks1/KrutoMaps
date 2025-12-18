package krutomaps.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import krutomaps.backend.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${security.jwt.secret}") String jwtSigningKey) {
        if (jwtSigningKey == null || jwtSigningKey.isBlank()) {
            throw new IllegalArgumentException("JWT signing key must be provided via 'security.jwt.secret'");
        }
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT signing key must be at least 256 bits after Base64 decoding");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String generateToken(UserDetails userDetails) {
        UserEntity user = (UserEntity) userDetails;
        Map<String, Object> extraClaims = Map.of(
                "id", user.getId(),
                "role", user.getRole().getRoleName()
        );

        Instant now = Instant.now();
        return Jwts.builder()
                .header().type("JWT").and()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofDays(1))))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            return username.equals(userDetails.getUsername()) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(Date.from(Instant.now()));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }


}
