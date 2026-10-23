package com.github.jaarmaco.messagemanager.security;

import java.util.Base64;

public abstract class JwtValidation {

    public static boolean isValidSecretKey(String key) {
        return key != null && !key.isBlank() && !key.isEmpty();
    }

    public static byte[] decodeSecretKey(String key) {
        return Base64.getDecoder().decode(key);
    }
}

