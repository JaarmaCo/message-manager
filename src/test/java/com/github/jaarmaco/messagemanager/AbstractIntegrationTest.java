package com.github.jaarmaco.messagemanager;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties = {
    "logging.level.com.github.jaarmaco.messagemanager=ERROR" 
})
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @BeforeAll
    static void setup() {
        postgres.start();
    } 

}

