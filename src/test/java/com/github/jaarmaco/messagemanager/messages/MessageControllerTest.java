package com.github.jaarmaco.messagemanager.messages;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import com.github.jaarmaco.messagemanager.security.JwtService;

@AutoConfigureMockMvc
public class MessageControllerTest extends AbstractIntegrationTest {


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
 
    private final UUID USER_ID = UUID.randomUUID();
    private final UUID GROUP_ID = UUID.randomUUID();
    private final UUID MESSAGE_ID = UUID.randomUUID();

    private final User USER = User.fromIdAndPasswordDigest(USER_ID, "pw");
    private final MessageGroup GROUP = MessageGroup.builder()
        .groupId(GROUP_ID)
        .groupName("Group")
        .build();
    private final String MESSAGE_TEXT = "The quick brown fox jumped over the lazy dog.";
    private final Message MESSAGE = Message.builder()
        .sender(USER)
        .group(GROUP)
        .messageId(MESSAGE_ID)
        .messageText(MESSAGE_TEXT)
        .build();

    private String jwt() {
        
        when(userRepository.findById(USER_ID))
            .thenReturn(Optional.of(USER));

        return jwtService.generateTokenForUser(USER);
    }

    @Nested
    class GetMessage {

        @Test
        void isAuthenticated() throws Exception {
            
            mockMvc.perform(get(prefix + "/messages/{messageId}", MESSAGE_ID))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void serializesToMessageResponse() throws Exception {

            when(userService.locateUser(any(Principal.class)))
                .thenReturn(Result.of(USER));

            when(messageService.readMessage(USER, MESSAGE_ID))
                .thenReturn(Result.of(MESSAGE));

            mockMvc.perform(get(prefix + "/messages/{messageId}", MESSAGE_ID)
                    .header("Authorization", "Bearer " + jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(MESSAGE_ID.toString()))
                .andExpect(jsonPath("$.plainTextMessage").value(MESSAGE_TEXT))
                .andExpect(jsonPath("$.senderUserId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.receiverGroupId").value(GROUP_ID.toString()));
        }
    }
}

