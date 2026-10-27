package com.github.jaarmaco.messagemanager.messages;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

import static org.hamcrest.core.StringContains.containsString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.jaarmaco.messagemanager.AbstractIntegrationTest;
import com.github.jaarmaco.messagemanager.auth.model.User;
import com.github.jaarmaco.messagemanager.auth.model.UserRepository;
import com.github.jaarmaco.messagemanager.auth.model.UserService;
import com.github.jaarmaco.messagemanager.lib.functional.Result;
import com.github.jaarmaco.messagemanager.messages.model.Message;
import com.github.jaarmaco.messagemanager.messages.model.MessageGroup;
import com.github.jaarmaco.messagemanager.messages.model.MessageService;
import com.github.jaarmaco.messagemanager.messages.requests.MessageGroupSendRequest;
import com.github.jaarmaco.messagemanager.security.JwtService;

@AutoConfigureMockMvc
public class MessageGroupControllerTest extends AbstractIntegrationTest {

    @Value("${app.api.prefix}")
    private String prefix;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MessageService messageService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID GROUP_ID = UUID.randomUUID();

    private final User USER = User.fromIdAndPasswordDigest(USER_ID, "pw");
    private final MessageGroup GROUP = MessageGroup.builder()
        .groupId(GROUP_ID)
        .groupName("Group")
        .build();
    private Message message(UUID messageId, String messageText) {
        return Message.builder()
            .sender(USER)
            .group(GROUP)
            .messageId(messageId)
            .messageText(messageText)
            .build();
    }

    private String jwt() {
        
        when(userRepository.findById(USER_ID))
            .thenReturn(Optional.of(USER));

        return jwtService.generateTokenForUser(USER);
    }

    @Nested
    class SubmitGroupMessage {
        
        @Test
        void isAuthenticated() throws Exception {
            
            var request = new MessageGroupSendRequest("Hello");
            mockMvc.perform(post(prefix + "/groups/{groupId}/post", GROUP_ID)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void answersWithCreated() throws Exception {

            final UUID MESSAGE_ID = UUID.randomUUID();

            final Message MESSAGE = Message.builder()
                .messageId(MESSAGE_ID)
                .build();

            when(userService.locateUser(any(Principal.class)))
                .thenReturn(Result.of(USER));

            when(messageService.submitMessage(USER, GROUP_ID, "Hello"))
                .thenReturn(Result.of(MESSAGE));

            var request = new MessageGroupSendRequest("Hello");
            mockMvc.perform(post(prefix + "/groups/{groupId}/post", GROUP_ID)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsBytes(request))
                    .header("Authorization", "Bearer " + jwt()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(prefix + "/messages/" + MESSAGE_ID)));
        }
    }

    @Nested
    class GetGroupMessages {

        @Test
        void isAuthenticated() throws Exception {
            mockMvc.perform(get(prefix + "/groups/{groupId}/messages", GROUP_ID)
                    .queryParam("pageNumber", "1"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void serializesToMessagePage() throws Exception {

            final UUID MESSAGE_1_ID = UUID.randomUUID();
            final UUID MESSAGE_2_ID = UUID.randomUUID();

            when(userService.locateUser(any(Principal.class)))
                .thenReturn(Result.of(USER));

            when(messageService.readMessages(eq(USER), eq(GROUP_ID), any(Pageable.class)))
                .thenReturn(Result.of(
                    List.of(
                        message(MESSAGE_1_ID, "1"),
                        message(MESSAGE_2_ID, "2"))));

            mockMvc.perform(get(prefix + "/groups/{groupId}/messages", GROUP_ID)
                    .header("Authorization", "Bearer " + jwt())
                    .queryParam("pageNumber", "1")
                    .queryParam("pageSize", "5"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.results[0].messageId").value(MESSAGE_1_ID.toString()))
                .andExpect(jsonPath("$.results[1].messageId").value(MESSAGE_2_ID.toString()));
        }
    }
}

