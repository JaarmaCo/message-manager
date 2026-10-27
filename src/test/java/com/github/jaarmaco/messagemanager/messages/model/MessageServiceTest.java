package com.github.jaarmaco.messagemanager.messages.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.github.jaarmaco.messagemanager.auth.model.User;
import com.github.jaarmaco.messagemanager.exceptions.ForbiddenResponseException;
import com.github.jaarmaco.messagemanager.exceptions.NotFoundResponseException;
import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;
import com.github.jaarmaco.messagemanager.lib.functional.Result;
import com.github.jaarmaco.messagemanager.network.NetworkService;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(MessageServiceTest.TestConfig.class)
public class MessageServiceTest {

    @MockitoBean
    private NetworkService networkService;

    @MockitoBean
    private MessageRepository messageRepository;

    @MockitoBean
    private MessageGroupRepository messageGroupRepository;

    @MockitoBean
    private MessageGroupParticipantRepository messageGroupParticipantRepository;

    @Autowired
    private MessageService messageService;

    @Configuration
    static class TestConfig {

        @Bean
        MessageService messageService(NetworkService networkService,
                                      MessageRepository messageRepository,
                                      MessageGroupRepository messageGroupRepository,
                                      MessageGroupParticipantRepository messageGroupParticipantRepository) {
            return MessageService
                .builder()
                .networkService(networkService)
                .messageRepository(messageRepository)
                .messageGroupRepository(messageGroupRepository)
                .messageGroupParticipantRepository(messageGroupParticipantRepository)
                .build();
        }
    }

    final UUID USER_ID = UUID.randomUUID();
    final UUID GROUP_ID = UUID.randomUUID();

    final String MESSAGE = "Hello";

    final User USER = User
        .builder()
        .userId(USER_ID)
        .build();

    final MessageGroup GROUP = MessageGroup
        .builder()
        .groupId(GROUP_ID)
        .groupName("Group 1")
        .build();

    MessageGroupParticipant participant(MessageGroupRole role) {
        return MessageGroupParticipant
        .builder()
        .key(MessageGroupParticipant.key(GROUP, USER))
        .role(role)
        .build();
    }

    @Nested
    class Submit {

        @Captor
        private ArgumentCaptor<Message> savedMessage;

        @Test
        void rejectsIfGroupDoesNotExist() {

            when(messageGroupRepository.findById(GROUP_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService
                    .submitMessage(USER, GROUP_ID, MESSAGE)
                    .throwIfHasException())
                .isInstanceOfSatisfying(NotFoundResponseException.class, exception -> {
                    assertThat(exception.getProblemDetail().getDetail())
                        .isEqualTo(ResponseExceptions.MESSAGE_GROUP_NOT_FOUND);
                });

            verifyNoInteractions(messageRepository);
        }

        @Test
        void rejectsIfNotInGroup() {

            when(messageGroupRepository.findById(GROUP_ID))
               .thenReturn(Optional.of(GROUP));

            when(messageGroupParticipantRepository.findById(any()))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService
                    .submitMessage(USER, GROUP_ID, MESSAGE)
                    .throwIfHasException())
                .isInstanceOfSatisfying(ForbiddenResponseException.class, exception -> {
                    assertThat(exception.getProblemDetail().getDetail())
                        .isEqualTo(ResponseExceptions.MEMBERSHIP_REQUIRED);
                });

            verifyNoInteractions(messageRepository);
        }

        @Test
        void rejectsIfIncompatibleRole() {

            final MessageGroupParticipant PARTICIPANT = participant(MessageGroupRole.guest());

            when(messageGroupRepository.findById(GROUP_ID))
               .thenReturn(Optional.of(GROUP));

            when(messageGroupParticipantRepository.findById(any()))
                .thenReturn(Optional.of(PARTICIPANT));

            assertThatThrownBy(() -> messageService
                    .submitMessage(USER, GROUP_ID, MESSAGE)
                    .throwIfHasException())
                .isInstanceOfSatisfying(ForbiddenResponseException.class, exception -> {
                    assertThat(exception.getProblemDetail().getDetail())
                        .isEqualTo(ResponseExceptions.ROLE_REQUIRED);
                });

            verifyNoInteractions(messageRepository);
        }

        @Test
        void persistsMessageAndForwardsMessageToNetwork() {

            final MessageGroupParticipant PARTICIPANT = participant(MessageGroupRole.participant());

            when(messageGroupRepository.findById(GROUP_ID))
               .thenReturn(Optional.of(GROUP));

            when(messageGroupParticipantRepository.findById(any()))
                .thenReturn(Optional.of(PARTICIPANT));

            doAnswer(args -> args.getArgument(0, Message.class))
                .when(messageRepository)
                .save(any(Message.class));

            doAnswer(_ -> Result.of(0))
                .when(networkService)
                .forwardMessage(any(Message.class));

            assertThat(messageService.submitMessage(USER, GROUP_ID, MESSAGE))
                .matches(Result::isSuccess, "Request should succeed without any errors");
     
            verify(messageRepository)
                .save(savedMessage.capture());

            Message message = savedMessage.getValue();

            assertThat(message)
                .extracting(Message::getMessageText)
                .isEqualTo(MESSAGE);

            assertThat(message)
                .extracting(Message::getSender)
                .extracting(User::getUserId)
                .isEqualTo(USER_ID);

            assertThat(message)
                .extracting(Message::getGroup)
                .extracting(MessageGroup::getGroupId)
                .isEqualTo(GROUP_ID);
        
            verify(networkService)
                .forwardMessage(message);
        }
    }

    @Nested
    class ReadMessages {

        @Test 
        void rejectsIfGroupIsMissing() {

            when(messageGroupRepository.findById(GROUP_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService
                    .readMessages(USER, GROUP_ID, PageRequest.ofSize(100))
                    .requireSuccess())
                .isInstanceOfSatisfying(NotFoundResponseException.class, exception -> {
                    assertThat(exception)
                        .extracting(NotFoundResponseException::getProblemDetail)
                        .extracting(ProblemDetail::getDetail)
                        .isEqualTo(ResponseExceptions.MESSAGE_GROUP_NOT_FOUND);
                });            
        }

        @Test
        void rejectsIfNotInGroup() {

            when(messageGroupRepository.findById(GROUP_ID))
                .thenReturn(Optional.of(GROUP));

            when(messageGroupParticipantRepository
                    .findById(MessageGroupParticipant.key(GROUP, USER)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService
                    .readMessages(USER, GROUP_ID, PageRequest.ofSize(100))
                    .requireSuccess())
                .isInstanceOfSatisfying(ForbiddenResponseException.class, exception -> {
                    assertThat(exception)
                        .extracting(ForbiddenResponseException::getProblemDetail)
                        .extracting(ProblemDetail::getDetail)
                        .isEqualTo(ResponseExceptions.MEMBERSHIP_REQUIRED);
                });
        }


        @Test
        void rejectsIfIncompatibleRole() {

            when(messageGroupRepository.findById(GROUP_ID))
                .thenReturn(Optional.of(GROUP));

            when(messageGroupParticipantRepository
                    .findById(MessageGroupParticipant.key(GROUP, USER)))
                .thenReturn(Optional.of(participant(MessageGroupRole.banned())));

            assertThatThrownBy(() -> messageService
                    .readMessages(USER, GROUP_ID, PageRequest.ofSize(100))
                    .requireSuccess())
                .isInstanceOfSatisfying(ForbiddenResponseException.class, exception -> {
                    assertThat(exception)
                        .extracting(ForbiddenResponseException::getProblemDetail)
                        .extracting(ProblemDetail::getDetail)
                        .isEqualTo(ResponseExceptions.ROLE_REQUIRED);
                });
        }

        @Test
        void fetchesPaginatedList() {

            final UUID MESSAGE_1_ID = UUID.randomUUID();
            final UUID MESSAGE_2_ID = UUID.randomUUID();

            final Message[] MESSAGES = {
                Message.builder()
                    .messageId(MESSAGE_1_ID)
                    .messageText("1")
                    .sender(USER)
                    .group(GROUP)
                    .build(),
                Message.builder()
                    .messageId(MESSAGE_2_ID)
                    .messageText("2")
                    .sender(USER)
                    .group(GROUP)
                    .build(),
            };

            final Pageable PAGEABLE = PageRequest.ofSize(100);

            when(messageGroupRepository.findById(GROUP_ID))
                .thenReturn(Optional.of(GROUP));

            when(messageGroupParticipantRepository
                    .findById(MessageGroupParticipant.key(GROUP, USER)))
                .thenReturn(Optional.of(participant(MessageGroupRole.participant())));

            doAnswer(args -> {
                return Optional.of(List.of(MESSAGES));
            }).when(messageRepository).findInGroup(GROUP, PAGEABLE);
            
            assertThat(messageService.readMessages(USER, GROUP_ID, PAGEABLE))
                .extracting(Result::requireSuccess)
                .asInstanceOf(iterable(Message.class))
                .containsExactlyInAnyOrder(MESSAGES);
            
            verify(messageRepository, never())
                .save(any());
        }
    }


    @Nested
    class ReadMessage {

        final UUID MESSAGE_ID = UUID.randomUUID();
        final String MESSAGE_TEXT = "text";
        final Message MESSAGE = Message.builder()
            .messageId(MESSAGE_ID)
            .messageText(MESSAGE_TEXT)
            .sender(USER)
            .group(GROUP)
            .build();

        @Test
        void rejectsIfMessageIsMissing() {

            when(messageRepository.findById(MESSAGE_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService
                    .readMessage(USER, MESSAGE_ID)
                    .requireSuccess())
                .isInstanceOfSatisfying(NotFoundResponseException.class, exception -> {
                    assertThat(exception)
                        .extracting(NotFoundResponseException::getProblemDetail)
                        .extracting(ProblemDetail::getDetail)
                        .isEqualTo(ResponseExceptions.MESSAGE_NOT_FOUND);
                });
        }

        @Test
        void rejectsIfNotInGroup() {

            when(messageRepository.findById(MESSAGE_ID))
                .thenReturn(Optional.of(MESSAGE));

            when(messageGroupParticipantRepository
                    .findById(MessageGroupParticipant.key(GROUP, USER)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService
                    .readMessage(USER, MESSAGE_ID)
                    .requireSuccess())
                .isInstanceOfSatisfying(ForbiddenResponseException.class, exception -> {
                    assertThat(exception)
                        .extracting(ForbiddenResponseException::getProblemDetail)
                        .extracting(ProblemDetail::getDetail)
                        .isEqualTo(ResponseExceptions.MEMBERSHIP_REQUIRED);
                });
        }


        @Test
        void rejectsIfIncompatibleRole() {

            when(messageRepository.findById(MESSAGE_ID))
                .thenReturn(Optional.of(MESSAGE));

            when(messageGroupParticipantRepository
                    .findById(MessageGroupParticipant.key(GROUP, USER)))
                .thenReturn(Optional.of(participant(MessageGroupRole.banned())));

            assertThatThrownBy(() -> messageService
                    .readMessage(USER, MESSAGE_ID)
                    .requireSuccess())
                .isInstanceOfSatisfying(ForbiddenResponseException.class, exception -> {
                    assertThat(exception)
                        .extracting(ForbiddenResponseException::getProblemDetail)
                        .extracting(ProblemDetail::getDetail)
                        .isEqualTo(ResponseExceptions.ROLE_REQUIRED);
                });
        }

        @Test
        void fetchesMessage() {

            when(messageRepository.findById(MESSAGE_ID))
                .thenReturn(Optional.of(MESSAGE));

            when(messageGroupParticipantRepository
                    .findById(MessageGroupParticipant.key(GROUP, USER)))
                .thenReturn(Optional.of(participant(MessageGroupRole.participant())));

            Message message = assertThat(messageService.readMessage(USER, MESSAGE_ID))
                .extracting(Result::requireSuccess)
                .actual();
    
            assertThat(message)
                .extracting(Message::getMessageId)
                .isEqualTo(MESSAGE_ID);

            assertThat(message)
                .extracting(Message::getMessageText)
                .isEqualTo(MESSAGE_TEXT);

            verify(messageRepository, never())
                .save(any());
        }
    }
}

