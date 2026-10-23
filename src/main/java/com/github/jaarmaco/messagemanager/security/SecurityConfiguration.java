package com.github.jaarmaco.messagemanager.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.github.jaarmaco.messagemanager.properties.CorsConfigurationProperties;
import com.github.jaarmaco.messagemanager.properties.ApiConfigurationProperties.Endpoint;
import com.github.jaarmaco.messagemanager.properties.ApiConfigurationProperties;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({ CorsConfigurationProperties.class, ApiConfigurationProperties.class })
public class SecurityConfiguration {

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private CorsConfigurationProperties corsConfigurationProperties;

    @Autowired
    private ApiConfigurationProperties apiConfigurationProperties;

    @Autowired
    private Environment environment;

    private RequestMatcher[] publicEndpointsMap;

    @PostConstruct
    void setUp() {

        List<RequestMatcher> publicEndpintMatchers = new ArrayList<>();
        for (Endpoint endpoint : apiConfigurationProperties.getPublicEndpoints()) {
            
            HttpMethod method = HttpMethod.valueOf(endpoint.getMethod());
            String path = environment.resolveRequiredPlaceholders(endpoint.getPath());
            
            RequestMatcher matcher = PathPatternRequestMatcher.pathPattern(method, path);
            publicEndpintMatchers.add(matcher);
        }
        publicEndpointsMap = publicEndpintMatchers.toArray(publicEndpointsMap);
    }

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
                    .requestMatchers(publicEndpointsMap).permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(handling -> handling
                    .authenticationEntryPoint(this::onAuthenticationFailure)
                    .accessDeniedHandler(this::onAccessDenied))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(corsConfigurationProperties.getAccessControlMaxAge());

        corsConfigurationProperties
            .getAllowedMethods()
            .forEach(corsConfiguration::addAllowedMethod);
        corsConfigurationProperties
            .getAllowedHeaders()
            .forEach(corsConfiguration::addAllowedHeader);
        corsConfigurationProperties
            .getAllowedOrigins()
            .forEach(corsConfiguration::addAllowedOrigin);

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

