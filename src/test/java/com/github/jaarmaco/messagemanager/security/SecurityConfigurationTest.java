package com.github.jaarmaco.messagemanager.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.jaarmaco.messagemanager.actuator.responses.ActuatorHealthResponse;
import com.github.jaarmaco.messagemanager.auth.model.User;
import com.github.jaarmaco.messagemanager.auth.model.UserRepository;

@SpringBootTest(properties = {
    "logging.level.com.github.jaarmaco.messagemanager=ERROR"
})
@AutoConfigureMockMvc
@Import(SecurityConfigurationTest.TestConfig.class)
public class SecurityConfigurationTest {

    @Value("${app.api.prefix}")
    private String prefix;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private UserRepository userRepository;
   
    @TestConfiguration
    @EnableAutoConfiguration(exclude = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class
    })
    static class TestConfig {}

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
        var user = User.fromIdAndPasswordDigest(userId, "aaaa");

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        String token = jwtService.generateTokenForUser(user);

        mockMvc.perform(
                get(prefix + "/actuator/authorized")
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                    .value(ActuatorHealthResponse.AUTHENTICATED_AS_PREFIX + userId.toString()));
    }
}

