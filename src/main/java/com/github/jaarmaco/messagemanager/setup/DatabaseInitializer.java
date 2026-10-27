package com.github.jaarmaco.messagemanager.setup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.github.jaarmaco.messagemanager.messages.model.MessageGroupRole;
import com.github.jaarmaco.messagemanager.messages.model.MessageGroupRoleRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@DependsOnDatabaseInitialization
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private MessageGroupRoleRepository messageGroupRoleRepository;

    @Transactional
    private void performUpdate() {
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

    @Override
    public void run(String... args) throws Exception {

        log.info("Populating database...");

        StopWatch watch = new StopWatch();
        watch.start();

        try {
            performUpdate();
        } catch (Exception ex) {
            log.error("Exception in database initializer: {}", ex);
            log.error("Database is in an inconsistent state due to the previous error. Will not continue, exiting now...");
            System.exit(1);
        }

        watch.stop();
        log.info("Database population completed in {} ms", watch.getTotalTimeMillis());
    }
}

