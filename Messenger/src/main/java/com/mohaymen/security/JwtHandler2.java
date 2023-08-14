//package com.mohaymen.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Component
//public class JwtHandler2 {
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @Value("${jwt.expiration.minutes}")
//    private int expirationMinutes;
//    @Value("${jwt.expiration.days}")
//    private int expirationDay;
//
//    public String generateToken(long id) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("id", id);
//        return Jwts.builder()
//                .setClaims(claims)
//                // .setSubject(subject)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + (long) expirationDay *24* 60 * 1000))
//                .signWith(getSigningKey())
//                .compact();
//    }
//
//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    public boolean isTokenExpired(String token) {
//        Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }
//
//    // Generate new token for user with 3 days expiration
//    public String refreshToken(String token) {
//        Claims claims = getAllClaimsFromToken(token);
//        claims.setExpiration(new Date(System.currentTimeMillis() + (long) expirationMinutes * 60 * 1000));
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .signWith(getSigningKey())
//                .compact();
//    }
//
//
//    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//    private Claims getAllClaimsFromToken(String token) {
//        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
//    }
//
//    private SecretKey getSigningKey() {
//        byte[] jwtSecretBytes = secret.getBytes();
//        return Keys.hmacShaKeyFor(jwtSecretBytes);
//    }
//}
