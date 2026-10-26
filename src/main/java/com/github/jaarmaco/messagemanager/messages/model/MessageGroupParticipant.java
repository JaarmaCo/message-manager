package com.github.jaarmaco.messagemanager.messages.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.github.jaarmaco.messagemanager.auth.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "message_group_participants")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageGroupParticipant { 

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key {

        @Getter
        @Setter
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "group_id")
        private MessageGroup messageGroup;

        @Getter
        @Setter
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private User user;
    }

    public static Key key(MessageGroup group, User user) {
        return new Key(group, user);
    }

    @EmbeddedId
    private Key key;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_name")
    private MessageGroupRole role;

    @Getter
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

