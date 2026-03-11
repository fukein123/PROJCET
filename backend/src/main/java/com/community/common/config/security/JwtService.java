package com.community.common.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtService {
  private final JwtProperties props;

  private SecretKey key() {
    return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String sign(Map<String, Object> claims) {
    var now = Instant.now();
    var exp = now.plusSeconds(props.getExpireSeconds());
    return Jwts.builder()
        .issuer(props.getIssuer())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claims(claims)
        .signWith(key())
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith(key())
        .requireIssuer(props.getIssuer())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}

