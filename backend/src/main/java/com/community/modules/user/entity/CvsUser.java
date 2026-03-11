package com.community.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("cvs_user")
public class CvsUser {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String username;
  private String passwordHash;
  private String email;
  private String phone;
  private String avatarUrl;
  private Integer gender;
  private String role;
  private Integer status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

