package com.github.jaarmaco.messagemanager.messages.model;

import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.jaarmaco.messagemanager.auth.model.User;
import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;
import com.github.jaarmaco.messagemanager.lib.functional.Result;
import com.github.jaarmaco.messagemanager.network.NetworkService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Service
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageService {

    @Autowired
    private MessageGroupRepository messageGroupRepository;

    @Autowired
    private MessageGroupParticipantRepository messageGroupParticipantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NetworkService networkService;

    public boolean rolePermitsPostingMessages(MessageGroupRole role) {
        return role != null && role.getLevel() != null && role.getLevel() >= 2;
    }

    public boolean rolePermitsReadingMessages(MessageGroupRole role) {
        return role != null && role.getLevel() != null && role.getLevel() >= 1;
    }

    @Transactional
    public Result<Message> submitMessage(@NonNull User sender,
                                         @NonNull UUID receiver,
                                         @NonNull String plainTextMessage) {
        
        return Result.doTry(() -> {

            MessageGroup messageGroup = messageGroupRepository
                .findById(receiver)
                .orElseThrow(ResponseExceptions::messageGroupNotFound);

            MessageGroupParticipant participant = messageGroupParticipantRepository
                .findById(MessageGroupParticipant.key(messageGroup, sender))
                .orElseThrow(ResponseExceptions::membershipRequired);

            if (!rolePermitsPostingMessages(participant.getRole())) {
                throw ResponseExceptions.roleRequired();
            }

            Message messageToSend = Message
                .builder()
                .group(messageGroup)
                .sender(sender)
                .messageText(plainTextMessage)
                .build();

            messageToSend = messageRepository.save(messageToSend);
            
            networkService
                .forwardMessage(messageToSend)
                .ignoringErrors();

            return messageToSend;
        });
    }

    @Transactional
    public Result<List<Message>> readMessages(@NonNull User self,
                                              @NonNull UUID groupId,
                                              @NonNull Pageable pageable) {

        return Result.doTry(() -> {

            MessageGroup messageGroup = messageGroupRepository
                .findById(groupId)
                .orElseThrow(ResponseExceptions::messageGroupNotFound);

            MessageGroupParticipant participant = messageGroupParticipantRepository
                .findById(MessageGroupParticipant.key(messageGroup, self))
                .orElseThrow(ResponseExceptions::membershipRequired);

            if (!rolePermitsReadingMessages(participant.getRole())) {
                throw ResponseExceptions.roleRequired();
            }

            List<Message> messages = messageRepository
                .findInGroup(messageGroup, pageable)
                .orElse(List.of());

            return messages;
        });
    }
}

