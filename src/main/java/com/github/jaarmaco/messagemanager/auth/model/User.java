package com.github.jaarmaco.messagemanager.auth.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Getter
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Getter
    @Setter
    @Column(name = "user_password_digest", nullable = false)
    private String userPasswordDigest;
    
    public static User fromIdAndPasswordDigest(UUID userId, String userPasswordDigest) {
        var user = new User();
        user.userId = userId;
        user.userPasswordDigest = userPasswordDigest;
        return user;
    }
}

