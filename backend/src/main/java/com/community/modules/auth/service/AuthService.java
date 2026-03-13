package com.community.modules.auth.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.JwtTokenUtil;
import com.community.modules.auth.dto.AuthTokenResponse;
import com.community.modules.auth.dto.LoginRequest;
import com.community.modules.auth.dto.RegisterRequest;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_AVATAR =
            "https://cdn.jsdelivr.net/gh/fukexin123/assets/default-avatar.png";
    private static final String LOGIN_FAILED_MESSAGE = "账号、密码或登录入口错误";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthTokenResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (user == null) {
            throw new BusinessException(LOGIN_FAILED_MESSAGE);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(LOGIN_FAILED_MESSAGE);
        }
        String requestedRole = StrUtil.trim(request.getRole());
        if (StrUtil.isNotBlank(requestedRole) && !StrUtil.equalsIgnoreCase(requestedRole, user.getRole())) {
            throw new BusinessException(LOGIN_FAILED_MESSAGE);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthTokenResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        if (!StrUtil.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(StrUtil.blankToDefault(request.getGender(), "UNKNOWN"));
        user.setAvatar(StrUtil.blankToDefault(request.getAvatar(), DEFAULT_AVATAR));
        user.setRealName(request.getRealName());
        user.setRole("VOLUNTEER");
        user.setStatus(1);
        user.setCertified(0);
        userMapper.insert(user);
    }
}
