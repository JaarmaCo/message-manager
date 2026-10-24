package com.github.jaarmaco.messagemanager.auth;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.jaarmaco.messagemanager.auth.model.User;
import com.github.jaarmaco.messagemanager.auth.model.UserService;
import com.github.jaarmaco.messagemanager.auth.requests.AuthLoginRequest;
import com.github.jaarmaco.messagemanager.auth.requests.AuthRegisterRequest;
import com.github.jaarmaco.messagemanager.auth.responses.AuthLoginResponse;
import com.github.jaarmaco.messagemanager.auth.responses.AuthUserResponse;
import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/whoami")
    public ResponseEntity<AuthUserResponse> getAuthenticatedUser(@AuthenticationPrincipal Principal principal) {
        
        User user = userService
            .locateUser(principal)
            .orElseThrow(ResponseExceptions::invalidUserCredentials);

        return ResponseEntity.ok(AuthUserResponse
                .builder()
                .userId(user.getUserId())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthLoginResponse> register(@RequestBody AuthRegisterRequest registerRequest) {

        // TODO: Add rate limiting (Bucket4j)
        User user = userService
            .createUserWithPassword(registerRequest.password())
            .requireSuccess();

        String token = userService
            .userAuthenticator(user)
            .authenticate(registerRequest.password())
            .requireSuccess();

        return ResponseEntity.ok(new AuthLoginResponse(user.getUserId(), token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(@RequestBody AuthLoginRequest loginRequest) {

        // TODO: Add other user location methods

        User user = userService
            .locateUser(loginRequest.user())
            .orElseThrow(ResponseExceptions::invalidUserCredentials);

        String token = userService
            .userAuthenticator(user)
            .authenticate(loginRequest.password())
            .orElseThrow(ResponseExceptions::invalidUserCredentials);

        return ResponseEntity.ok(new AuthLoginResponse(user.getUserId(), token)); 
    }
}

