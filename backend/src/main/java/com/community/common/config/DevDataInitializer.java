package com.community.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.modules.user.entity.CvsUser;
import com.community.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    var admin = userMapper.selectOne(new LambdaQueryWrapper<CvsUser>()
        .eq(CvsUser::getUsername, "admin"));
    if (admin == null) {
      CvsUser u = new CvsUser();
      u.setUsername("admin");
      u.setPasswordHash(passwordEncoder.encode("Admin@123456"));
      u.setEmail("admin@example.com");
      u.setRole("ADMIN");
      u.setStatus(1);
      userMapper.insert(u);
      return;
    }
    if (admin.getPasswordHash() == null || admin.getPasswordHash().contains("PLACEHOLDER")) {
      admin.setPasswordHash(passwordEncoder.encode("Admin@123456"));
      userMapper.updateById(admin);
    }
  }
}

