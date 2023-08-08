package com.mohaymen.noName;

import io.jsonwebtoken.Jwts;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class ss {

    public static void main(String[] args) {
        String jwtToken = Jwts.builder()
                .claim("name", "Jane Doe")
                .claim("email", "jane@example.com")
                .setSubject("jane")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(5l, ChronoUnit.MINUTES)))
                .compact();
        System.out.println("woooooooooooooo");
        System.out.println(jwtToken);
//        int saltLength = 16; // Length of the salt in bytes
//
//        // Create a secure random number generator
//        SecureRandom random = new SecureRandom();
//
//        // Generate a random salt
//        byte[] salt = new byte[saltLength];
//        random.nextBytes(salt);
//
//        // Print the salt as a hexadecimal string
//        String saltString = bytesToHex(salt);
//        System.out.println("Salt: " + saltString);
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
