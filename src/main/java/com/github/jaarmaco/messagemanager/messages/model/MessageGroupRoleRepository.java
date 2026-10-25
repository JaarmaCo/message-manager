package com.github.jaarmaco.messagemanager.messages.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageGroupRoleRepository extends JpaRepository<MessageGroupRole, String> {}

