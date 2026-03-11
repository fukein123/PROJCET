package com.community.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.config.security.JwtService;
import com.community.common.exception.BizException;
import com.community.modules.user.dto.LoginRequest;
import com.community.modules.user.dto.LoginResponse;
import com.community.modules.user.dto.RegisterRequest;
import com.community.modules.user.dto.UserMeResponse;
import com.community.modules.user.entity.CvsUser;
import com.community.modules.user.mapper.UserMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public LoginResponse login(LoginRequest req) {
    var user = userMapper.selectOne(new LambdaQueryWrapper<CvsUser>()
        .eq(CvsUser::getUsername, req.getUsername()));
    if (user == null || user.getStatus() == null || user.getStatus() == 0) {
      throw new BizException(401, "账号或密码错误");
    }
    if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
      throw new BizException(401, "账号或密码错误");
    }
    if (req.getLoginType() != null && !req.getLoginType().isBlank()) {
      // 简单约束：管理员入口只允许 ADMIN/COMMUNITY_ADMIN；志愿者入口只允许 VOLUNTEER
      var t = req.getLoginType().trim().toUpperCase();
      if ("ADMIN".equals(t) && !("ADMIN".equals(user.getRole()) || "COMMUNITY_ADMIN".equals(user.getRole()))) {
        throw new BizException(403, "角色不匹配");
      }
      if ("VOLUNTEER".equals(t) && !"VOLUNTEER".equals(user.getRole())) {
        throw new BizException(403, "角色不匹配");
      }
    }

    var token = jwtService.sign(Map.of(
        "uid", user.getId(),
        "username", user.getUsername(),
        "role", user.getRole()
    ));
    return new LoginResponse(token, toMe(user));
  }

  @Transactional
  public UserMeResponse registerVolunteer(RegisterRequest req) {
    var exists = userMapper.selectCount(new LambdaQueryWrapper<CvsUser>()
        .eq(CvsUser::getUsername, req.getUsername()));
    if (exists != null && exists > 0) {
      throw new BizException(409, "用户名已存在");
    }
    CvsUser u = new CvsUser();
    u.setUsername(req.getUsername());
    u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
    u.setEmail(req.getEmail());
    u.setPhone(req.getPhone());
    u.setAvatarUrl(req.getAvatarUrl());
    u.setGender(req.getGender());
    u.setRole("VOLUNTEER");
    u.setStatus(1);
    userMapper.insert(u);
    return toMe(u);
  }

  public UserMeResponse toMe(CvsUser u) {
    return new UserMeResponse(
        u.getId(),
        u.getUsername(),
        u.getEmail(),
        u.getPhone(),
        u.getAvatarUrl(),
        u.getGender(),
        u.getRole()
    );
  }
}

