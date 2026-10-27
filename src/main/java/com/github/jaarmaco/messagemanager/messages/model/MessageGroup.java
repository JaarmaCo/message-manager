package com.github.jaarmaco.messagemanager.messages.model;

import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message_group")
public class MessageGroup {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "group_id")
    @EqualsAndHashCode.Include
    private UUID groupId;
    
    @Getter
    @Setter
    @Column(name = "group_name")
    private String groupName;
    
    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Set<MessageGroupParticipant> participants;

    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Set<Message> messages;
}


