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
@ConfigurationProperties("app.api")
public class ApiConfigurationProperties {

    private String domainName = "localhost:8080";
    
    private String prefix = "/api/v1";

    private List<Endpoint> publicEndpoints = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Endpoint {
        private String method;
        private String path;
    }
}

