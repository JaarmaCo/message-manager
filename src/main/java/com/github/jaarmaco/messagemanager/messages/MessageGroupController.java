package com.github.jaarmaco.messagemanager.messages;

import java.util.List;
import java.net.URI;
import java.security.Principal;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.github.jaarmaco.messagemanager.auth.model.User;
import com.github.jaarmaco.messagemanager.auth.model.UserService;
import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;
import com.github.jaarmaco.messagemanager.lib.pagination.Paginated;
import com.github.jaarmaco.messagemanager.lib.pagination.Pagination;
import com.github.jaarmaco.messagemanager.messages.model.Message;
import com.github.jaarmaco.messagemanager.messages.model.MessageService;
import com.github.jaarmaco.messagemanager.messages.requests.MessageGroupSendRequest;
import com.github.jaarmaco.messagemanager.messages.responses.MessageGetResponse;

@RestController
@RequestMapping("${app.api.prefix}/groups")
public class MessageGroupController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @PostMapping("/{groupId}/post")
    public ResponseEntity<?> sendMessageToGroup(@PathVariable UUID groupId,
                                                @RequestBody MessageGroupSendRequest request,
                                                @AuthenticationPrincipal Principal principal) {
        User user = userService
            .locateUser(principal)
            .orElseThrow(ResponseExceptions::invalidUserCredentials);

        Message message = messageService
            .submitMessage(user, groupId, request.plainTextMessage())
            .requireSuccess();

        URI messageUri = MvcUriComponentsBuilder
            .fromController(MessageController.class)
            .path("/{messageId}")
            .buildAndExpand(message.getMessageId())
            .toUri();

        return ResponseEntity.created(messageUri).build();
    }

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<Paginated<MessageGetResponse>> getGroupMessages(@PathVariable UUID groupId,
                                                                          @RequestParam Integer pageNumber,
                                                                          @RequestParam Integer pageSize,
                                                                          @AuthenticationPrincipal Principal principal) {
        User user = userService
            .locateUser(principal)
            .orElseThrow(ResponseExceptions::invalidUserCredentials);

        Pageable pageable = Pagination
            .ofNullableRange(pageNumber, pageSize)
            .requireSuccess();

        List<MessageGetResponse> groupMessages = messageService
            .readMessages(user, groupId, pageable)
            .requireSuccess()
            .stream()
            .map(MessageGetResponse::fromMessage)
            .toList();

        return ResponseEntity.ok(Paginated.fromPaginatedResults(groupMessages, pageable));
    }
}

