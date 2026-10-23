package com.github.jaarmaco.messagemanager.security;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.github.jaarmaco.messagemanager.properties.JwtConfigurationProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
@EnableConfigurationProperties(JwtConfigurationProperties.class)
public class JwtService {

    public static final String CLAIM_USER_ID = "uid";
    public static final int KEY_MIN_SIZE_BYTES = 32;

    @Autowired
    private JwtConfigurationProperties jwtConfigurationProperties;

    private SecretKey secretKey;

    @PostConstruct
    private void setUp() {
        
        if (!JwtValidation.isValidSecretKey(jwtConfigurationProperties.getSecret())) {
            throw new RuntimeException("Invalid secret JWT secret key.");
        }

        if (jwtConfigurationProperties.getExpiration() <= 0) {
            throw new RuntimeException("Expiration time has to be positive.");
        }

        byte[] decodedKey = JwtValidation.decodeSecretKey(jwtConfigurationProperties.getSecret());
        if (decodedKey.length < KEY_MIN_SIZE_BYTES) {
            throw new RuntimeException("Decoded key is smaller than the required min length " + KEY_MIN_SIZE_BYTES);
        }

        secretKey = Keys.hmacShaKeyFor(decodedKey);
    }

    public String generateTokenForUser(UUID userId) {

        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null.");
        }

        String idStr = userId.toString();
        
        var now = new Date();
        var expiration = new Date(now.getTime() + jwtConfigurationProperties.getExpiration()); 
        
        return Jwts
            .builder()
            .subject(idStr)
            .claim(CLAIM_USER_ID, idStr)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    public Claims parseToken(String jwt) {
        
        if (jwt == null || jwt.isBlank()) {
            throw new IllegalArgumentException("JWT cannot be blank or null");
        }
        
        JwtParser parser = Jwts
            .parser()
            .verifyWith(secretKey)
            .build();
        return parser
            .parseSignedClaims(jwt)
            .getPayload();
    }

    public Optional<Claims> tryParseToken(String jwt) {
        try {
            return Optional.of(parseToken(jwt));
        } catch (Throwable e) {
            return Optional.empty();
        }
    }
}

