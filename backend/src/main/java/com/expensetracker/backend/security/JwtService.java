package com.expensetracker.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "expense_tracker_super_secret_key_2026_1234567890";

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(
                    SECRET.getBytes(StandardCharsets.UTF_8)
            );

    // 24 hours
    private static final long EXPIRATION =
            1000L * 60 * 60 * 24;

    // ==============================
    // GENERATE TOKEN
    // ==============================

    public String generateToken(String email) {

        Date issuedAt = new Date();
        Date expiration =
                new Date(issuedAt.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // ==============================
    // EXTRACT EMAIL
    // ==============================

    public String extractEmail(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // ==============================
    // VALIDATE TOKEN
    // ==============================

    public boolean isValid(String token) {

        try {

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();

            Date expiration = claims.getExpiration();

            System.out.println("========== JWT CHECK ==========");
            System.out.println("JWT EMAIL      : " + email);
            System.out.println("JWT EXPIRES AT : " + expiration);
            System.out.println("CURRENT TIME   : " + new Date());
            System.out.println(
                    "JWT NOT EXPIRED: "
                            + expiration.after(new Date())
            );
            System.out.println("===============================");

            return email != null
                    && !email.isBlank()
                    && expiration.after(new Date());

        } catch (Exception e) {

            System.out.println("========== JWT INVALID ==========");
            System.out.println(
                    "ERROR TYPE    : "
                            + e.getClass().getName()
            );
            System.out.println(
                    "ERROR MESSAGE : "
                            + e.getMessage()
            );
            System.out.println("=================================");

            return false;
        }
    }
}