package com.github.jaarmaco.messagemanager.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockFilterChain filterChain;

    private SecurityContext context;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = spy(new MockFilterChain());
        context = spy(SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.setContext(context);
    }
    
    @Test
    void ignoresOptionMethods() throws Exception {

        request.setMethod("OPTIONS");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain)
            .doFilter(request, response);

        verifyNoInteractions(context);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doesNotAuthenticateIfAuthorizationHeaderIsNotSet() throws Exception {

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain)
            .doFilter(request, response);
 
        verify(context, never())
            .setAuthentication(any());

        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticatesWhenBearerIsSet() throws Exception {

        String mockJwt = "abc123";
        request.addHeader("Authorization", "Bearer " + mockJwt);
        
        Claims claims = Jwts
            .claims()
            .subject("userId")
            .add(JwtService.CLAIM_USER_ID, "userId")
            .build();

        when(jwtService.tryParseToken(mockJwt))
            .thenReturn(Optional.of(claims));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(context)
            .setAuthentication(captor.capture());
        
        assertThat(captor.getValue())
            .asInstanceOf(type(UsernamePasswordAuthenticationToken.class))
            .extracting(UsernamePasswordAuthenticationToken::getPrincipal)
            .asInstanceOf(type(Principal.class))
            .extracting(Principal::getName)
            .isEqualTo("userId");
    
        verify(filterChain)
            .doFilter(request, response);
    }

    @Test
    void rejectsInvalidTokens() throws Exception {
        
        String mockJwt = "abc123";
        request.addHeader("Authorization", "Bearer " + mockJwt);

        when(jwtService.tryParseToken(mockJwt))
            .thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain)
            .doFilter(request, response);

        verify(context, never())
            .setAuthentication(any());;
    }
}

