package com.community.common.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cvs.security.jwt")
public class JwtProperties {
  private String issuer;
  private String secret;
  private long expireSeconds;
}

