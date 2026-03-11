package spring.boot.cardprocessing.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration; // milliseconds

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(Long userId) {
    return Jwts.builder()
        .subject(userId.toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  public Long extractUserId(String token) {
    Claims claims = extractAllClaims(token);
    return Long.parseLong(claims.getSubject());
  }

  public boolean isTokenValid(String token) {
    try {
      extractAllClaims(token);
      return true;
    } catch (Exception e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }



}
