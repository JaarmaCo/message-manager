package com.github.jaarmaco.messagemanager.messages.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageGroupRepository extends JpaRepository<MessageGroup, UUID> {}

