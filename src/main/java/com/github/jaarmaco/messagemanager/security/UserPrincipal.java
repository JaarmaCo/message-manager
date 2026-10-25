package com.github.jaarmaco.messagemanager.security;

import java.security.Principal;

import com.github.jaarmaco.messagemanager.auth.model.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserPrincipal implements Principal {

    private final User user;

    @Override
    public String getName() {
        return user.getUserId().toString();
    }
}

