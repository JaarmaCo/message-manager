package com.github.jaarmaco.messagemanager.actuator;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.jaarmaco.messagemanager.actuator.requests.ActuatorEchoRequest;
import com.github.jaarmaco.messagemanager.actuator.responses.ActuatorEchoResponse;
import com.github.jaarmaco.messagemanager.actuator.responses.ActuatorHealthResponse;
import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${app.api.prefix}/actuator")
public class ActuatorController {

    @GetMapping("/health")
    public ResponseEntity<ActuatorHealthResponse> checkHealth() {
        return ResponseEntity.ok(new ActuatorHealthResponse());
    }

    @PostMapping("/echo")
    public ResponseEntity<ActuatorEchoResponse> echo(@RequestBody ActuatorEchoRequest request) {
        return ResponseEntity.ok(ActuatorEchoResponse.ofRequest(request));
    }

    @GetMapping("/authorized")
    public ResponseEntity<ActuatorHealthResponse> checkAuthorized(@AuthenticationPrincipal Principal principal) {
        if (principal == null) {
            log.warn("Unauthorized invocation to /actuator/authorized passed security filters.");
            throw ResponseExceptions.forbidden();
        }
        return ResponseEntity.ok(ActuatorHealthResponse
                .withMessage("Authenticated as " + principal.getName()));
    }

    @GetMapping("/forbidden")
    public ResponseEntity<?> checkForbidden() {
        throw ResponseExceptions.forbidden("/actuator/forbidden is always forbidden by design.");
    }
}

