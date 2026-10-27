package com.github.jaarmaco.messagemanager.messages;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.jaarmaco.messagemanager.messages.responses.MessageGetResponse;

@RestController
@RequestMapping("${app.api.prefix}/messages")
public class MessageController {

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageGetResponse> getMessage(@PathVariable UUID messageId,
                                                         @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.notFound().build();
    }
}

