package com.example.api_gateway.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
public class JwtUtil {
    private final String jwtSecret = "bXkgcGVyZmVjdCBzZWNyZXQga2V5IHdpdGggYWRkaXRpb25hbCBzZWNyZXQ=";

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Error extracting username from token: " + e.getMessage());
            return null;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractRole(String token) {
        try {
            return (String) getClaims(token).get("role");
        } catch (JwtException e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        try {
            return ((Number) getClaims(token).get("userId")).longValue();
        } catch (JwtException | NullPointerException e) {
            return null;
        }
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Error extracting username from token: " + e.getMessage());
            return false;
        }
    }
}

