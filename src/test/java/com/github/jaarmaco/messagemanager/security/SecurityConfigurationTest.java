package com.github.jaarmaco.messagemanager.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.github.jaarmaco.messagemanager.actuator.responses.ActuatorHealthResponse;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigurationTest {

    @Value("${app.api.prefix}")
    private String prefix;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {}

    @Test
    void healthCheckPasses() throws Exception {
        mockMvc.perform(get(prefix + "/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                    .value(ActuatorHealthResponse.DEFAULT_MESSAGE));
    }

    @Test
    void unauthorizedRequestIsDenied() throws Exception {
        mockMvc.perform(get(prefix + "/actuator/authorized"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizedRequestIsAccepted() throws Exception {
        
        var userId = UUID.randomUUID();

        String token = jwtService.generateTokenForUser(userId);

        mockMvc.perform(
                get(prefix + "/actuator/authorized")
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                    .value(ActuatorHealthResponse.AUTHENTICATED_AS_PREFIX + userId.toString()));
    }
}

