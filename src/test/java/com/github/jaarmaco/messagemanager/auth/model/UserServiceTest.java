package com.github.jaarmaco.messagemanager.auth.model;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.github.jaarmaco.messagemanager.lib.functional.Result;
import com.github.jaarmaco.messagemanager.security.JwtService;
import com.github.jaarmaco.messagemanager.security.UserPrincipal;

@SpringBootTest
public class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Test
    void authenticationRejectsMismatchingPasswords() {
        
        final var USER_ID = UUID.randomUUID();

        String password = "password";
        String passwordDigest = passwordEncoder.encode(password);

        var user = User.fromIdAndPasswordDigest(USER_ID, passwordDigest);

        assertThat(userService
                .userAuthenticator(user)
                .authenticate("mismatching password"))
            .matches(Result::isFailure);
    }

    @Test
    void authenticationAcceptsMatchingPasswords() {

        final var USER_ID = UUID.randomUUID();

        String password = "password";
        String passwordDigest = passwordEncoder.encode(password);

        var user = User.fromIdAndPasswordDigest(USER_ID, passwordDigest);
       
        when(jwtService.generateTokenForUser(user))
            .thenReturn("token");

        assertThat(userService
                .userAuthenticator(user)
                .authenticate(password))
            .extracting(Result::requireSuccess)
            .isEqualTo("token");
    }

    @Test
    void findsExistingUserFromId() {

        final var USER_ID = UUID.randomUUID();

        var user = User.fromIdAndPasswordDigest(USER_ID, passwordEncoder.encode("password"));

        when(userRepository.findById(USER_ID))
            .thenReturn(Optional.of(user));

        assertThat(userService
                .locateUser(USER_ID))
            .matches(Result::isSuccess);
    }

    @Test
    void findsExistingUserFromUserPrincipal() {

        final var USER_ID = UUID.randomUUID();

        var user = User.fromIdAndPasswordDigest(USER_ID, passwordEncoder.encode("password"));

        when(userRepository.findById(USER_ID))
            .thenReturn(Optional.of(user));

        Principal principal = new UserPrincipal(user);

        assertThat(userService
                .locateUser(principal))
            .matches(Result::isSuccess);
    }
}

