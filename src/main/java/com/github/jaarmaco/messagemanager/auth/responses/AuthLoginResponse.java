package com.github.jaarmaco.messagemanager.auth.responses;

import java.util.UUID;

public record AuthLoginResponse(UUID userId, String token) {}

