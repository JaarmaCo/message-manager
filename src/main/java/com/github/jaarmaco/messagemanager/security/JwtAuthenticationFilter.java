package com.github.jaarmaco.messagemanager.security;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.jaarmaco.messagemanager.auth.model.UserRepository;
import com.github.jaarmaco.messagemanager.lib.functional.Result;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            
            filterChain.doFilter(request, response);
            return;
        }

        requestToken(request)
            .ifPresent(jwt -> authenticate(jwt, request));

        filterChain.doFilter(request, response);
    }

    private void authenticate(String jwt, HttpServletRequest request) {

        Result
            .fromOptional(jwtService.tryParseToken(jwt))
            .map(claims -> claims.get(JwtService.CLAIM_USER_ID, String.class))
            .map(UUID::fromString)
            .andThenFromOptional(userRepository::findById)
            .onSuccess(user -> { 

                Principal principal = new UserPrincipal(user);

                Object credentials = null;
                Collection<? extends GrantedAuthority> grantedAuthorities = List.of();
                var authentication = new UsernamePasswordAuthenticationToken(principal, credentials, grantedAuthorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            });
    }

    private Optional<String> requestToken(HttpServletRequest request) {
        
        String authHeaderValue = request.getHeader("Authorization");
        
        if (authHeaderValue == null || !authHeaderValue.startsWith("Bearer ")) {
            return Optional.empty();
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return Optional.empty();
        }

        return Optional.of(authHeaderValue.substring("Bearer ".length()));
    }
}

