package com.github.jaarmaco.messagemanager.openapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenApiDocumentProvider {

    private final RestClient restClient;

    OpenApiDocumentProvider(@Value("${app.openapi.base-url}") String baseUrl) {
        restClient = RestClient
            .builder()
            .baseUrl(baseUrl)
            .build();
    }

    public String getGeneratedOpenApiAsString() { 
        return restClient
            .get()
            .uri("/v3/api-docs")
            .retrieve()
            .body(String.class);
    }
}

