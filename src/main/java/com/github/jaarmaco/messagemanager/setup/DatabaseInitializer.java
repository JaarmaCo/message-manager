package com.github.jaarmaco.messagemanager.setup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.github.jaarmaco.messagemanager.messages.model.MessageGroupRole;
import com.github.jaarmaco.messagemanager.messages.model.MessageGroupRoleRepository;

import jakarta.transaction.Transactional;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private MessageGroupRoleRepository messageGroupRoleRepository;

    @Transactional
    @Override
    public void run(String... args) throws Exception {

        for (MessageGroupRole role : MessageGroupRole.values()) {
            messageGroupRoleRepository
                .findById(role.getName())
                .ifPresentOrElse(_ -> {
                    // continue
                }, () -> {
                    messageGroupRoleRepository.save(role);
                });
        }
    }
}

