package com.mohaymen.noName;

import java.security.SecureRandom;

public class ss {

    public static void main(String[] args) {
        int saltLength = 16; // Length of the salt in bytes

        // Create a secure random number generator
        SecureRandom random = new SecureRandom();

        // Generate a random salt
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);

        // Print the salt as a hexadecimal string
        String saltString = bytesToHex(salt);
        System.out.println("Salt: " + saltString);
    }

    // Helper method to convert byte array to hexadecimal string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}
