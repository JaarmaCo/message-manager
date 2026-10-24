package com.github.jaarmaco.messagemanager.auth.model;

import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;
import com.github.jaarmaco.messagemanager.lib.functional.Result;
import com.github.jaarmaco.messagemanager.security.JwtService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Result<User> createUserWithPassword(String userPassword) {
        return Result.doTry(() -> {

            String encodedPassword = passwordEncoder.encode(userPassword);

            var user = new User();
            user.setUserPasswordDigest(encodedPassword);
            
            return userRepository.saveAndFlush(user);
        });
    }

    public Result<User> locateUser(UUID userId) {
        return Result.fromOptional(userRepository.findById(userId));
    }

    public Result<User> locateUser(Principal userPrincipal) {
        return Result
            .ofNullable(userPrincipal)
            .map(Principal::getName)
            .map(UUID::fromString)
            .andThen(this::locateUser);
    }
    
    public UserAuthenticator userAuthenticator(@NonNull User user) {
        return new UserAuthenticator(user);
    }

    @RequiredArgsConstructor
    public class UserAuthenticator {

        private final User targetUser;

        public boolean matches(String password) {
            if (password == null) {
                return false;
            }
            return passwordEncoder.matches(password, targetUser.getUserPasswordDigest());
        }

        public Result<String> authenticate(String password) {
            if (!matches(password)) {
                return Result.exception(ResponseExceptions.forbidden("Invalid user credentials"));
            }
            return Result.doTry(() ->
                    jwtService.generateTokenForUser(targetUser.getUserId()));
        }
    } 
}

