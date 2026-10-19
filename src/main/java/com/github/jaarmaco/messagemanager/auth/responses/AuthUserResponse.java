package com.github.jaarmaco.messagemanager.auth.responses;

import java.util.UUID;

import lombok.Builder;

@Builder
public record AuthUserResponse(
    UUID userId,
    String firstName,
    String surname
) {}

