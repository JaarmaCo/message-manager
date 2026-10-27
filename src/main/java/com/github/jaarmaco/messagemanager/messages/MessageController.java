package com.github.jaarmaco.messagemanager.messages;

import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.jaarmaco.messagemanager.auth.model.User;
import com.github.jaarmaco.messagemanager.auth.model.UserService;
import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;
import com.github.jaarmaco.messagemanager.messages.model.Message;
import com.github.jaarmaco.messagemanager.messages.model.MessageService;
import com.github.jaarmaco.messagemanager.messages.responses.MessageGetResponse;

@RestController
@RequestMapping("${app.api.prefix}/messages")
public class MessageController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageGetResponse> getMessage(@PathVariable UUID messageId,
                                                         @AuthenticationPrincipal Principal principal) {
        User user = userService
            .locateUser(principal)
            .orElseThrow(ResponseExceptions::invalidUserCredentials);

        Message message = messageService
            .readMessage(user, messageId)
            .requireSuccess();

        return ResponseEntity.ok(MessageGetResponse.fromMessage(message));
    }
}

