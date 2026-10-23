package com.github.jaarmaco.messagemanager.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("app.cors")
public class CorsConfigurationProperties {

    private List<String> allowedOrigins = new ArrayList<>();

    private List<String> allowedMethods =
        new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    private List<String> allowedHeaders =
        new ArrayList<>(List.of("Authorization", "Content-Type"));

    private long accessControlMaxAge = 60L;
}

