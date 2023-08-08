package com.mohaymen.noName;

import java.security.SecureRandom;

public class Salt {
    private static final int saltLength = 10;
    private static final SecureRandom secureRandom = new SecureRandom();

    public static byte[] getSaltArray() {
        byte[] salt = new byte[saltLength];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
