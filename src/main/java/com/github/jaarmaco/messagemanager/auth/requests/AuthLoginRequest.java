package com.github.jaarmaco.messagemanager.auth.requests;

public record AuthLoginRequest(
    UserAuthenticationMethod method,
    String user,
    String credentials
) {}

