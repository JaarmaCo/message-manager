package com.github.jaarmaco.messagemanager.messages.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("select m from Message m where m.group = ?1 order by m.createdAt desc")
    Optional<List<Message>> findInGroup(MessageGroup group, Pageable pageable);
}

