package com.github.jaarmaco.messagemanager.messages.responses;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.jaarmaco.messagemanager.messages.model.Message;

import lombok.Builder;

@Builder
public record MessageGetResponse(
        UUID messageId,
        UUID senderUserId,
        UUID receiverGroupId,
        LocalDateTime sentAt,
        LocalDateTime modifiedAt,
        String plainTextMessage) {

    public static MessageGetResponse fromMessage(Message message) {
        return MessageGetResponse
            .builder()
            .messageId(message.getMessageId())
            .senderUserId(message.getSender().getUserId())
            .receiverGroupId(message.getGroup().getGroupId())
            .sentAt(message.getCreatedAt())
            .modifiedAt(message.getUpdatedAt())
            .plainTextMessage(message.getMessageText())
            .build();
    }
}

