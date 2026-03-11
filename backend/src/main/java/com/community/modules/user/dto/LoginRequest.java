package com.community.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank
  private String username;
  @NotBlank
  private String password;
  @NotBlank
  private String loginType; // ADMIN / VOLUNTEER（预留 COMMUNITY_ADMIN）
}

