package com.github.jaarmaco.messagemanager.actuator.responses;

import com.github.jaarmaco.messagemanager.actuator.requests.ActuatorEchoRequest;

public record ActuatorEchoResponse(String message) {
    public static ActuatorEchoResponse ofRequest(ActuatorEchoRequest request) {
        return new ActuatorEchoResponse(request.message());
    }
}

