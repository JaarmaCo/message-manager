package com.github.jaarmaco.messagemanager.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
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
    
        log.info("Initializing security configuration."); 

        try {
            List<RequestMatcher> publicEndpintMatchers = new ArrayList<>();
            for (Endpoint endpoint : apiConfigurationProperties.getPublicEndpoints()) {
            
                log.info("| Exposing {} {}", endpoint.getMethod(), endpoint.getPath());

                HttpMethod method = HttpMethod.valueOf(endpoint.getMethod());
                String path = endpoint.getPath();

                try {
                    path = environment.resolveRequiredPlaceholders(path);
                } catch (Throwable e) {

                    log.error("  | Failed to expand pattern '{}' with {}", endpoint.getPath(), e.getMessage());
                    publicEndpintMatchers.add(PathPatternRequestMatcher.pathPattern(method, path));
                    log.warn("  | Skipping expansion due to previous error. Endpoint {} {} may not behave as expected.",
                            endpoint.getMethod(), endpoint.getPath());
                    continue;
                }

                log.info("  | Environment expansion yields absolute path: {}", path);

                RequestMatcher matcher = PathPatternRequestMatcher.pathPattern(method, path);
                publicEndpintMatchers.add(matcher);

                log.info("  | Adding pattern {} {}", method.name(), path);
            }
            publicEndpointsMap = publicEndpintMatchers.toArray(RequestMatcher[]::new);

            log.info("Marked {} endpoints as unauthenticated/public.", publicEndpointsMap.length);
        } catch (Throwable e) {
            log.error("Critical error in intialization routine for the application security configuration; uncaught exception {}", e);
            log.error("Security configuration cannot boot, error is unrecoverable. Exiting now.");
            System.exit(1);
        }
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

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
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
 
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), problem);
    }
}

