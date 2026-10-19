package com.hospital.medicos.security;

import com.hospital.medicos.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
  private final SecretKey key;
  private final long expiration;

  public JwtUtils(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiration = expiration;
  }

  public String generateJwtToken(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer("clinica-aurora")
        .subject(user.getEmail())
        .claim("version", user.getTokenVersion())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expiration)))
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public Claims validateJwtToken(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .requireIssuer("clinica-aurora")
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long expirationSeconds() {
    return expiration / 1000;
  }
}
