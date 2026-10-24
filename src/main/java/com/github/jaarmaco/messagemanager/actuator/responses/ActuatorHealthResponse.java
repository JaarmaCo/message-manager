package com.github.jaarmaco.messagemanager.actuator.responses;

import lombok.NonNull;

public record ActuatorHealthResponse(String message) {

    public static String DEFAULT_MESSAGE = "Server is responsive.";
    public static String AUTHENTICATED_AS_PREFIX = "Authenticated as ";

    public static String authenticatedAsMessage(@NonNull String name) {
        return AUTHENTICATED_AS_PREFIX + name;
    }

    public static String parseAuthenticationNameFromMessage(@NonNull String message) {
        if (!message.startsWith(AUTHENTICATED_AS_PREFIX)) {
            throw new IllegalArgumentException("Missing expected prefix.");
        }
        return message.substring(AUTHENTICATED_AS_PREFIX.length());
    }

    public ActuatorHealthResponse() {
        this(DEFAULT_MESSAGE);
    }

    public static ActuatorHealthResponse withMessage(@NonNull String message) {
        return new ActuatorHealthResponse(message);
    }

    public static ActuatorHealthResponse withAuthenticatedAsMessage(String name) {
        return withMessage(authenticatedAsMessage(name));
    }
}

