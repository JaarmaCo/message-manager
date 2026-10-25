package com.github.jaarmaco.messagemanager.messages.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {}

