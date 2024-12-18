package io.github.dav033.user_service.services;

import io.github.dav033.user_service.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration-ms}")
    private long expiration;

    @Value("${application.security.jwt.refresh-token}")
    private long refreshTokenExpiration;


    public String generateToken(final User user) {
        return buildToken(user, expiration);
    }

    public String extractUsername(final String token) {
        final Claims jwtToken = Jwts.parser().verifyWith(getSignToken()).build().parseSignedClaims(token).getPayload();

        return jwtToken.getSubject();
    }

    public String generateRefreshToken(final User user) {
        return buildToken(user, refreshTokenExpiration);
    }


    private String buildToken(final User user, final long expiration) {

        return Jwts.builder()
                .id(user.getId().toString())
                .claims(Map.of("name", user.getName()))
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignToken())
                .compact();

    }

    public boolean isTokenValid(final String token, final User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getEmail()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(final String token) {
        final Claims jwtToken = Jwts.parser().verifyWith(getSignToken()).build().parseSignedClaims(token).getPayload();

        return jwtToken.getExpiration();
    }


    private SecretKey getSignToken() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }


}

