package com.superball.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Long userId, String role, String profileImageUrl,String nickname,Integer quizPoints) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("nickname", nickname)
                .claim("profileImageUrl", profileImageUrl)
                .claim("quizPoints", quizPoints)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String tokenOrHeader) {
        String token = tokenOrHeader.replace("Bearer ", "");
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String tokenOrHeader) {
        return Long.valueOf(extractSubject(tokenOrHeader));
    }

    public String extractRole(String tokenOrHeader) {
        String token = tokenOrHeader.replace("Bearer ", "");
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
