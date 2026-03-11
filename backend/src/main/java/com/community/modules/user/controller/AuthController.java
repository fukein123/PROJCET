package com.community.modules.user.controller;

import com.community.common.web.R;
import com.community.modules.user.dto.LoginRequest;
import com.community.modules.user.dto.LoginResponse;
import com.community.modules.user.dto.RegisterRequest;
import com.community.modules.user.dto.UserMeResponse;
import com.community.modules.user.entity.CvsUser;
import com.community.modules.user.mapper.UserMapper;
import com.community.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final UserService userService;
  private final UserMapper userMapper;

  @PostMapping("/login")
  public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    return R.ok(userService.login(req));
  }

  @PostMapping("/register")
  public R<UserMeResponse> register(@Valid @RequestBody RegisterRequest req) {
    return R.ok(userService.registerVolunteer(req));
  }

  @GetMapping("/me")
  public R<UserMeResponse> me() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
      return R.fail(401, "未登录");
    }
    var user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CvsUser>()
        .eq(CvsUser::getUsername, auth.getName()));
    if (user == null) {
      return R.fail(404, "用户不存在");
    }
    return R.ok(userService.toMe(user));
  }
}

