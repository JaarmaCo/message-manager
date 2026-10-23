package com.github.jaarmaco.messagemanager.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringBootTest(
    classes = JwtServiceTest.Configuration.class,
    properties = {
        "app.jwt.secret=VGhlcmUgYW5kIGJhY2sgYWdhaW4uIEEgSG9iYml0J3MgdGFsZSBieSBCaWxibyBCYWdnaW5zLgo=",
        "app.jwt.expiration=86400000"
    })
public class JwtServiceTest {

    @Autowired
    JwtService jwtService;

    @TestConfiguration
    @Import({
        JwtService.class
    })
    static class Configuration {}

    @Test
    void contextLoads() {}

    @Test
    void generateRejectsNullArguments() {
        
        assertThatThrownBy(() -> jwtService.generateTokenForUser(null))
            .as("Passing null to generateTokenForUser(UUID) should fail with IllegalArgumentException")
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseRejectsNullAndBlankStrings() {
        
        assertThatThrownBy(() -> jwtService.parseToken(null))
            .as("Passing null to parseToken(String) should fail with IllegalArgumentException")
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> jwtService.parseToken(""))
            .as("Passing an empty string to parseToken(String) should fail with IllegalArgumentException")
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> jwtService.parseToken("  "))
            .as("Passing a blank string to parseToken(String) should fail with IllegalArgumentException")
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generatedTokensCanBeParsed() {

        var userId = UUID.randomUUID();
        
        String jwt = jwtService.generateTokenForUser(userId);
        
        assertThat(jwt)
            .isNotNull()
            .isNotBlank();
    
        assertThat(jwtService.tryParseToken(jwt))
            .map(claims -> claims.get(JwtService.CLAIM_USER_ID))
            .isPresent()
            .get()
            .asString()
            .isEqualToIgnoringCase(userId.toString());
    }

    @Test
    void rejectsGarbageString() {

        assertThat(jwtService.tryParseToken("2abe33=99444bfgx"))
            .isEmpty();
    }

    @Test
    void rejectsInvalidKey() {

        SecretKey secretKey = Keys.hmacShaKeyFor("The quick brown fox jumped over the lazy dog.".getBytes());

        var now = new Date();
        var then = new Date(now.getTime() + 10000L);

        String jwt = Jwts
            .builder()
            .claim(JwtService.CLAIM_USER_ID, UUID.randomUUID())
            .issuedAt(now)
            .expiration(then)
            .signWith(secretKey)
            .compact();

        assertThat(jwtService.tryParseToken(jwt))
            .as("JWT not issued by the service should fail to parse.")
            .isEmpty();
    }

    // TODO: Key expiration test
}

