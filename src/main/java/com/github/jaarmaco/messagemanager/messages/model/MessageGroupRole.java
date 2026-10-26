package com.github.jaarmaco.messagemanager.messages.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "message_group_role")
public class MessageGroupRole {

    @Id
    @Getter
    @Column(name = "role_name")
    private String name;

    @Getter
    @Setter
    @Column(name = "role_level", nullable = false)
    private Integer level;
    
    @Getter
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Getter
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static MessageGroupRole admin() {
        return MessageGroupRole
            .builder()
            .name("admin")
            .level(4)
            .build();
    }

    public static MessageGroupRole owner() {
        return MessageGroupRole
            .builder()
            .name("owner")
            .level(3)
            .build();
    }

    public static MessageGroupRole participant() {
        return MessageGroupRole
            .builder()
            .name("participant")
            .level(2)
            .build();
    }

    public static MessageGroupRole guest() {
        return MessageGroupRole
            .builder()
            .name("guest")
            .level(1)
            .build();
    }

    public static List<MessageGroupRole> values() {
        return List.of(admin(), owner(), participant(), guest());
    }
}

