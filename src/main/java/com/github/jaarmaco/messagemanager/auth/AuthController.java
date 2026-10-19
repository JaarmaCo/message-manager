package com.github.jaarmaco.messagemanager.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.jaarmaco.messagemanager.auth.requests.AuthLoginRequest;
import com.github.jaarmaco.messagemanager.auth.responses.AuthLoginResponse;
import com.github.jaarmaco.messagemanager.auth.responses.AuthUserResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/whoami")
    public ResponseEntity<AuthUserResponse> getAuthenticatedUser() {
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(@RequestBody AuthLoginRequest loginRequest) {
        return ResponseEntity.badRequest().build();
    }
}

