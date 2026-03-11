package com.community.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserMeResponse {
  private Long id;
  private String username;
  private String email;
  private String phone;
  private String avatarUrl;
  private Integer gender;
  private String role;
}

