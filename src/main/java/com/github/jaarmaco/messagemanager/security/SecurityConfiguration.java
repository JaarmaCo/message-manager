package com.github.jaarmaco.messagemanager.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JsonMapper jsonMapper;

    @Value("${app.cors.allowed-origins:[]}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:[\"GET\",\"POST\",\"PUT\",\"DELETE\",\"OPTIONS\"]}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:[\"Authorization\",\"Content-Type\"]}")
    private String allowedHeaders;

    @Value("${app.cors.access-control-max-age:60}")
    private long accessControlMaxAge;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .httpBasic(basic -> basic.disable())
            .formLogin(login -> login.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                    .anyRequest().authenticated())
            .exceptionHandling(handling -> handling
                    .authenticationEntryPoint(this::onAuthenticationFailure)
                    .accessDeniedHandler(this::onAccessDenied))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        
        var listType = new TypeReference<List<String>>() {};

        List<String> allowedOriginUris;
        try {
            allowedOriginUris = jsonMapper.readValue(allowedOrigins, listType);
        } catch (Throwable e) {
            allowedOriginUris = List.of();
        }

        List<String> allowedMethodsList;
        try {
            allowedMethodsList = jsonMapper.readValue(allowedMethods, listType);
        } catch (Throwable e) {
            allowedMethodsList = List.of();
        }

        List<String> allowedHeadersList;
        try {
            allowedHeadersList = jsonMapper.readValue(allowedHeaders, listType);
        } catch (Throwable e) {
            allowedHeadersList = List.of();
        }

        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(accessControlMaxAge);

        allowedMethodsList.forEach(corsConfiguration::addAllowedMethod);
        allowedHeadersList.forEach(corsConfiguration::addAllowedHeader);
        allowedOriginUris.forEach(corsConfiguration::addAllowedOrigin);

        var corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("*/*", corsConfiguration);
        
        return corsConfigurationSource;
    }

    private void onAuthenticationFailure(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Throwable exception) throws IOException {

        var problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.UNAUTHORIZED, "This endpoint requires that you submit a valid JWT.");
        problem.setTitle("Authorization required.");
    
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), problem);
    }

    private void onAccessDenied(HttpServletRequest request,
                                HttpServletResponse response,
                                Throwable exception) throws IOException {

        var problem = ProblemDetail
            .forStatusAndDetail(HttpStatus.FORBIDDEN, "You are not allowed to access this endpoint.");
        problem.setTitle("Access denied.");
 
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), problem);
    }
}

