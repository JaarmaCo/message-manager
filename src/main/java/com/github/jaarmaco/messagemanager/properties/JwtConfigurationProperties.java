package com.github.jaarmaco.messagemanager.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("app.jwt")
public class JwtConfigurationProperties {

    private String secret;

    @Builder.Default
    private long expiration = 24L * 60L * 60L * 1000L; 
}

