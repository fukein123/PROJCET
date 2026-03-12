package com.community.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.user.dto.PasswordUpdateRequest;
import com.community.modules.user.dto.UserCreateRequest;
import com.community.modules.user.dto.UserUpdateRequest;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_AVATAR =
            "https://cdn.jsdelivr.net/gh/fukexin123/assets/default-avatar.png";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<User> pageUsers(long current, long size, String role, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(role), User::getRole, role)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword)
                        .or()
                        .like(User::getPhone, keyword))
                .orderByDesc(User::getCreateTime);
        PageHelper.startPage((int) current, (int) size);
        List<User> records = userMapper.selectList(wrapper);
        records.forEach(record -> record.setPassword(null));
        PageInfo<User> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public void createVolunteer(UserCreateRequest request) {
        ensureUsernameAvailable(request.getUsername(), null);
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(StringUtils.hasText(request.getGender()) ? request.getGender() : "UNKNOWN");
        user.setAvatar(StringUtils.hasText(request.getAvatar()) ? request.getAvatar() : DEFAULT_AVATAR);
        user.setRole("VOLUNTEER");
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setCertified(request.getCertified() == null ? 0 : request.getCertified());
        userMapper.insert(user);
    }

    public User getCurrentUser() {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    public void updateCurrentUser(UserUpdateRequest request) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        User dbUser = userMapper.selectById(userId);
        if (dbUser == null) {
            throw new BusinessException(404, "用户不存在");
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
            throw new BusinessException(401, "用户未登录");
        }
        User dbUser = userMapper.selectById(userId);
        if (dbUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), dbUser.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        dbUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(dbUser);
    }

    public void adminUpdateUser(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
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

    public void deleteVolunteer(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return;
        }
        if (!"VOLUNTEER".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException("仅允许删除志愿者账号");
        }
        userMapper.deleteById(id);
    }

    public void batchDeleteVolunteers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(this::deleteVolunteer);
    }

    private void ensureUsernameAvailable(String username, Long excludeUserId) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .ne(excludeUserId != null, User::getId, excludeUserId));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }
}
