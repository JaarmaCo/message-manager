package com.github.jaarmaco.messagemanager;

import org.springframework.boot.SpringApplication;

public class TestMessageManagerApplication {

	public static void main(String[] args) {
		SpringApplication
            .from(MessageManagerApplication::main)
            .with(TestcontainersConfiguration.class)
            .run(args);
	}

}
