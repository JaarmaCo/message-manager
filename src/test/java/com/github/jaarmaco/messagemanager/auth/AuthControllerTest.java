package com.github.jaarmaco.messagemanager.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.jaarmaco.messagemanager.AbstractIntegrationTest;
import com.github.jaarmaco.messagemanager.auth.model.User;
import com.github.jaarmaco.messagemanager.auth.model.UserRepository;
import com.github.jaarmaco.messagemanager.auth.model.UserService;
import com.github.jaarmaco.messagemanager.auth.requests.AuthLoginRequest;
import com.github.jaarmaco.messagemanager.auth.requests.AuthRegisterRequest;
import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;
import com.github.jaarmaco.messagemanager.lib.functional.Result;
import com.github.jaarmaco.messagemanager.security.JwtService;

import tools.jackson.databind.json.JsonMapper;

@AutoConfigureMockMvc
public class AuthControllerTest extends AbstractIntegrationTest {

    @Value("${app.api.prefix}")
    private String prefix;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;
    
    @Test
    void canGetAuthenticatedUser() throws Exception {

        var userId = UUID.randomUUID();
        var user = User.fromIdAndPasswordDigest(userId, "aaaa");
       
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        when(userService.locateUser(any(Principal.class)))
            .thenReturn(Result.of(user));

        String token = jwtService.generateTokenForUser(user);

        mockMvc.perform(get(prefix + "/auth/whoami")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void identityEndpointRejectsUnauthenticated() throws Exception {

        mockMvc.perform(get(prefix + "/auth/whoami"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUserIsUnauthenticatedAndReturnsJwt() throws Exception {

        String password = "password";
        var userId = UUID.randomUUID();
        var user = User.fromIdAndPasswordDigest(userId, "pw");
 
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        when(userService.createUserWithPassword(password))
            .thenReturn(Result.of(user));

        var authenticator = mock(UserService.UserAuthenticator.class);

        when(userService.userAuthenticator(user))
            .thenReturn(authenticator);

        when(authenticator.authenticate(password))
            .thenReturn(Result.of("jwt"));

        mockMvc.perform(post(prefix + "/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(new AuthRegisterRequest(password))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void loginUserIsUnauthenticatedAndReturnsJwtWhenPasswordMatches() throws Exception {

        String password = "password";
        var userId = UUID.randomUUID();
        var user = User.fromIdAndPasswordDigest(userId, "pw");

        when(userService.locateUser(userId))
            .thenReturn(Result.of(user));

        var authenticator = mock(UserService.UserAuthenticator.class);

        when(userService.userAuthenticator(user))
            .thenReturn(authenticator);
    
        when(authenticator.authenticate(password))
            .thenReturn(Result.of("jwt"));

        mockMvc.perform(post(prefix + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(new AuthLoginRequest(userId, password))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void loginUserIsRejectedWhenUserDoesNotExist() throws Exception {

        String password = "password";
        var userId = UUID.randomUUID();

        when(userService.locateUser(userId))
            .thenReturn(Result.empty());
 
        mockMvc.perform(post(prefix + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(new AuthLoginRequest(userId, password))))   
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.detail").value(ResponseExceptions.MESSAGE_INVALID_USER_CREDENTIALS));
    }

    @Test
    void loginUserIsRejectedWhenPasswordDoesNotMatch() throws Exception {

        String password = "password";
        var userId = UUID.randomUUID();
        var user = User.fromIdAndPasswordDigest(userId, "pw");

        when(userService.locateUser(userId))
            .thenReturn(Result.of(user));

        var authenticator = mock(UserService.UserAuthenticator.class);

        when(userService.userAuthenticator(user))
            .thenReturn(authenticator);

        when(authenticator.authenticate(password))
            .thenReturn(Result.empty());

        mockMvc.perform(post(prefix + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(new AuthLoginRequest(userId, password))))   
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.detail").value(ResponseExceptions.MESSAGE_INVALID_USER_CREDENTIALS));
    }
}

