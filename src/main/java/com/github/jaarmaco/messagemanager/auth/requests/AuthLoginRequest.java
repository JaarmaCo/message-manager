package com.github.jaarmaco.messagemanager.auth.requests;

import java.util.UUID;

public record AuthLoginRequest(
    UUID user,
    String password
) {}

