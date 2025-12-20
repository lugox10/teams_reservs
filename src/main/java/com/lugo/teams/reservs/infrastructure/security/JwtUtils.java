package com.lugo.teams.reservs.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret:}")
    private String jwtSecretBase64;

    private SecretKey key;
    private final long jwtExpirationMs = 24 * 60 * 60 * 1000L; // 24h

    @PostConstruct
    public void init() {
        if (jwtSecretBase64 == null || jwtSecretBase64.isBlank()) {
            throw new IllegalStateException("JWT secret no configurado. Pon JWT_SECRET en variables de entorno o application.properties");
        }
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretBase64);
        if (keyBytes.length < 64) {
            throw new IllegalStateException("JWT_SECRET inválido: debe ser 64 bytes (512 bits) cuando se decodifica base64. keyBytes.length=" + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetailsImpl user) {
        String role = user.getAuthorities().iterator().next().getAuthority();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
