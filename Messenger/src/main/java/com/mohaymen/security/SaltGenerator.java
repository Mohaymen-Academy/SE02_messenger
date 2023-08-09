package com.mohaymen.security;

import java.security.SecureRandom;

public class SaltGenerator {

    private static final int saltLength = 10;

    private static final SecureRandom secureRandom = new SecureRandom();

    public static byte[] getSaltArray() {
        byte[] salt = new byte[saltLength];
        secureRandom.nextBytes(salt);
        return salt;
    }

}
