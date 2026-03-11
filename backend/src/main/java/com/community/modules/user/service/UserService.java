package com.community.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.user.dto.PasswordUpdateRequest;
import com.community.modules.user.dto.UserUpdateRequest;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<User> pageUsers(long current, long size, String role, String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(role), User::getRole, role)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword)
                        .or()
                        .like(User::getPhone, keyword))
                .orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(record -> record.setPassword(null));
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public User getCurrentUser() {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "User not authenticated");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        user.setPassword(null);
        return user;
    }

    public void updateCurrentUser(UserUpdateRequest request) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "User not authenticated");
        }
        User dbUser = userMapper.selectById(userId);
        if (dbUser == null) {
            throw new BusinessException(404, "User not found");
        }
        if (!dbUser.getUsername().equals(request.getUsername())) {
            ensureUsernameAvailable(request.getUsername(), userId);
        }
        dbUser.setUsername(request.getUsername());
        dbUser.setEmail(request.getEmail());
        dbUser.setPhone(request.getPhone());
        dbUser.setGender(request.getGender());
        dbUser.setAvatar(request.getAvatar());
        dbUser.setRealName(request.getRealName());
        userMapper.updateById(dbUser);
    }

    public void updatePassword(PasswordUpdateRequest request) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "User not authenticated");
        }
        User dbUser = userMapper.selectById(userId);
        if (dbUser == null) {
            throw new BusinessException(404, "User not found");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), dbUser.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }
        dbUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(dbUser);
    }

    public void adminUpdateUser(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        if (!user.getUsername().equals(request.getUsername())) {
            ensureUsernameAvailable(request.getUsername(), id);
        }
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setAvatar(request.getAvatar());
        user.setRealName(request.getRealName());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole());
        }
        if (request.getCertified() != null) {
            user.setCertified(request.getCertified());
        }
        userMapper.updateById(user);
    }

    private void ensureUsernameAvailable(String username, Long excludeUserId) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .ne(excludeUserId != null, User::getId, excludeUserId));
        if (count != null && count > 0) {
            throw new BusinessException("Username already exists");
        }
    }
}
