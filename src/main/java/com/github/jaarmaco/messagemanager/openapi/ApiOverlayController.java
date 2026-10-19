package com.github.jaarmaco.messagemanager.openapi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webfuzzing.overlayjvm.OverlayJVM;

@RestController
@RequestMapping("/v3/api-docs-overlaid")
public class ApiOverlayController {

    @Value("classpath:openapi/documentation.overlay.yaml")
    private Resource documentationOverlay;

    @Autowired
    private OpenApiDocumentProvider openApiDocumentProvider;

    @GetMapping
    public ResponseEntity<?> getApi() {
        String documentationOverlayContents;
        try {
            documentationOverlayContents = documentationOverlay.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return ResponseEntity
                .internalServerError()
                .build();
        }
        
        String generatedOpenApi = openApiDocumentProvider.getGeneratedOpenApiAsString();
        String result = OverlayJVM
            .applyOverlay(generatedOpenApi, documentationOverlayContents)
            .transformedSchema;
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .contentLength(result.length())
            .body(result);
    }
}

