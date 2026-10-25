package com.github.jaarmaco.messagemanager.messages.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

