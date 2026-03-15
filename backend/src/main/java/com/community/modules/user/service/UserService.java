package com.community.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.entity.ActivityCheckRecord;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.content.entity.CommentInfo;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.mapper.CommentInfoMapper;
import com.community.modules.content.mapper.FavoriteActivityMapper;
import com.community.modules.content.mapper.ForumPostMapper;
import com.community.modules.user.dto.PasswordUpdateRequest;
import com.community.modules.user.dto.UserCreateRequest;
import com.community.modules.user.dto.UserUpdateRequest;
import com.community.modules.user.dto.VolunteerCertificationAuditRequest;
import com.community.modules.user.dto.VolunteerCertificationSubmitRequest;
import com.community.modules.user.entity.PointsChangeLog;
import com.community.modules.user.entity.User;
import com.community.modules.user.entity.VolunteerCertification;
import com.community.modules.user.mapper.PointsChangeLogMapper;
import com.community.modules.user.mapper.UserMapper;
import com.community.modules.user.mapper.VolunteerCertificationMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_AVATAR =
            "https://cdn.jsdelivr.net/gh/fukexin123/assets/default-avatar.png";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_VOLUNTEER = "VOLUNTEER";
    private static final String CERT_STATUS_NOT_SUBMITTED = "NOT_SUBMITTED";
    private static final String CERT_STATUS_PENDING = "PENDING";
    private static final String CERT_STATUS_APPROVED = "APPROVED";
    private static final String CERT_STATUS_REJECTED = "REJECTED";

    private final UserMapper userMapper;
    private final VolunteerCertificationMapper volunteerCertificationMapper;
    private final ActivityApplicationMapper activityApplicationMapper;
    private final ActivityCheckRecordMapper activityCheckRecordMapper;
    private final ForumPostMapper forumPostMapper;
    private final CommentInfoMapper commentInfoMapper;
    private final FavoriteActivityMapper favoriteActivityMapper;
    private final PointsChangeLogMapper pointsChangeLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminOperationLogService adminOperationLogService;

    public PageResult<User> pageUsers(long current,
                                      long size,
                                      String role,
                                      String keyword,
                                      Integer status,
                                      Integer certified,
                                      String certificationStatus) {
        String normalizedCertificationStatus = normalizeCertificationStatusFilter(certificationStatus);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(role), User::getRole, role)
                .eq(status != null, User::getStatus, status)
                .eq(certified != null, User::getCertified, certified)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword)
                        .or()
                        .like(User::getPhone, keyword))
                .orderByDesc(User::getCreateTime);
        if (StringUtils.hasText(normalizedCertificationStatus)
                && (!StringUtils.hasText(role) || ROLE_VOLUNTEER.equalsIgnoreCase(role))) {
            if (CERT_STATUS_NOT_SUBMITTED.equals(normalizedCertificationStatus)) {
                wrapper.apply("NOT EXISTS (SELECT 1 FROM volunteer_certification vc WHERE vc.user_id = sys_user.id)");
            } else {
                wrapper.apply(
                        "EXISTS (SELECT 1 FROM volunteer_certification vc WHERE vc.user_id = sys_user.id AND vc.status = {0})",
                        normalizedCertificationStatus
                );
            }
        }
        PageHelper.startPage((int) current, (int) size);
        List<User> records = userMapper.selectList(wrapper);
        enrichUsersWithCertification(records, true);
        records.forEach(this::sanitizeListUser);
        PageInfo<User> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public User getUserDetail(Long id) {
        User user = requireUser(id);
        enrichUsersWithCertification(List.of(user), false);
        sanitizeDetailUser(user);
        return user;
    }

    public void createUser(UserCreateRequest request) {
        String username = requireText(request.getUsername(), "Username is required");
        String password = requireText(request.getPassword(), "Password is required");
        String realName = requireText(request.getRealName(), "Real name is required");
        String phone = requireText(request.getPhone(), "Phone is required");
        ensureUsernameAvailable(username, null);

        String role = normalizeRole(request.getRole(), ROLE_VOLUNTEER);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setEmail(normalizeOptionalText(request.getEmail()));
        user.setPhone(phone);
        user.setGender(StringUtils.hasText(request.getGender()) ? request.getGender().trim() : "UNKNOWN");
        user.setAvatar(StringUtils.hasText(request.getAvatar()) ? request.getAvatar().trim() : DEFAULT_AVATAR);
        user.setRole(role);
        user.setPoints(0);
        user.setStatus(normalizeBinaryValue(request.getStatus(), 1, "Status is invalid"));
        user.setCertified(0);
        userMapper.insert(user);
    }

    public User getCurrentUser() {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "User is not logged in");
        }
        User user = requireUser(userId);
        enrichUsersWithCertification(List.of(user), false);
        sanitizeDetailUser(user);
        return user;
    }

    public void updateCurrentUser(UserUpdateRequest request) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "User is not logged in");
        }
        User dbUser = requireUser(userId);
        String username = requireText(request.getUsername(), "Username is required");
        if (!dbUser.getUsername().equals(username)) {
            ensureUsernameAvailable(username, userId);
        }
        dbUser.setUsername(username);
        dbUser.setEmail(normalizeOptionalText(request.getEmail()));
        dbUser.setPhone(normalizeOptionalText(request.getPhone()));
        dbUser.setGender(normalizeOptionalText(request.getGender()));
        dbUser.setAvatar(normalizeOptionalText(request.getAvatar()));
        dbUser.setRealName(normalizeOptionalText(request.getRealName()));
        userMapper.updateById(dbUser);
    }

    public void updatePassword(PasswordUpdateRequest request) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "User is not logged in");
        }
        User dbUser = requireUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), dbUser.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }
        dbUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(dbUser);
    }

    public void adminUpdateUser(Long id, UserUpdateRequest request) {
        User user = requireUser(id);
        String username = requireText(request.getUsername(), "Username is required");
        if (!user.getUsername().equals(username)) {
            ensureUsernameAvailable(username, id);
        }

        user.setUsername(username);
        user.setEmail(normalizeOptionalText(request.getEmail()));
        user.setPhone(normalizeOptionalText(request.getPhone()));
        user.setGender(normalizeOptionalText(request.getGender()));
        user.setAvatar(normalizeOptionalText(request.getAvatar()));
        user.setRealName(normalizeOptionalText(request.getRealName()));

        if (request.getStatus() != null) {
            if (Objects.equals(SecurityUtil.currentUserId(), id) && request.getStatus() == 0) {
                throw new BusinessException("Current login user cannot be disabled");
            }
            user.setStatus(normalizeBinaryValue(request.getStatus(), user.getStatus(), "Status is invalid"));
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(normalizeRole(request.getRole(), user.getRole()));
        }
        userMapper.updateById(user);
    }

    public void disableUser(Long id) {
        User user = requireUser(id);
        if (Objects.equals(SecurityUtil.currentUserId(), id)) {
            throw new BusinessException("Current login user cannot be disabled");
        }
        user.setStatus(0);
        userMapper.updateById(user);
        adminOperationLogService.record("DISABLE_USER", "USER", user.getId(), user.getUsername(), "status=0");
    }

    public void enableUser(Long id) {
        User user = requireUser(id);
        user.setStatus(1);
        userMapper.updateById(user);
    }

    public void batchDisableUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(this::disableUser);
    }

    public void batchEnableUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(this::enableUser);
    }

    public void deleteVolunteer(Long id) {
        User user = requireVolunteerUser(id);
        if (Objects.equals(SecurityUtil.currentUserId(), id)) {
            throw new BusinessException("Current login user cannot be deleted");
        }

        activityApplicationMapper.delete(new LambdaQueryWrapper<ActivityApplication>()
                .eq(ActivityApplication::getUserId, id));
        activityCheckRecordMapper.delete(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getUserId, id));
        forumPostMapper.delete(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getUserId, id));
        commentInfoMapper.delete(new LambdaQueryWrapper<CommentInfo>()
                .eq(CommentInfo::getUserId, id));
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, id));
        volunteerCertificationMapper.delete(new LambdaQueryWrapper<VolunteerCertification>()
                .eq(VolunteerCertification::getUserId, id));
        pointsChangeLogMapper.delete(new LambdaQueryWrapper<PointsChangeLog>()
                .eq(PointsChangeLog::getUserId, id));
        userMapper.deleteById(id);

        adminOperationLogService.record("DELETE_USER", "USER", user.getId(), user.getUsername(), "role=VOLUNTEER");
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

    public VolunteerCertification getCurrentUserCertification() {
        User user = requireCurrentVolunteer();
        return buildCertificationView(user, findCertificationByUserId(user.getId()));
    }

    public void submitCurrentUserCertification(VolunteerCertificationSubmitRequest request) {
        User user = requireCurrentVolunteer();
        VolunteerCertification certification = findCertificationByUserId(user.getId());
        String currentStatus = resolveCertificationStatus(user, certification);
        if (CERT_STATUS_PENDING.equals(currentStatus)) {
            throw new BusinessException("Certification is under review");
        }
        LocalDateTime now = LocalDateTime.now();
        if (certification == null) {
            certification = new VolunteerCertification();
            certification.setUserId(user.getId());
        }
        certification.setRealName(requireText(request.getRealName(), "Real name is required"));
        certification.setIdCardNo(normalizeIdCardNo(request.getIdCardNo()));
        certification.setIdCardFrontUrl(requireText(request.getIdCardFrontUrl(), "ID card front image is required"));
        certification.setIdCardBackUrl(requireText(request.getIdCardBackUrl(), "ID card back image is required"));
        certification.setStatus(CERT_STATUS_PENDING);
        certification.setRejectReason(null);
        certification.setSubmitTime(now);
        certification.setAuditTime(null);
        certification.setAuditorId(null);

        if (certification.getId() == null) {
            volunteerCertificationMapper.insert(certification);
        } else {
            volunteerCertificationMapper.updateById(certification);
        }

        user.setRealName(certification.getRealName());
        user.setCertified(0);
        userMapper.updateById(user);
    }

    public VolunteerCertification getUserCertificationDetail(Long id) {
        User user = requireVolunteerUser(id);
        return buildCertificationView(user, findCertificationByUserId(id));
    }

    public void auditUserCertification(Long id, VolunteerCertificationAuditRequest request) {
        User user = requireVolunteerUser(id);
        VolunteerCertification certification = findCertificationByUserId(id);
        if (certification == null) {
            throw new BusinessException(404, "Certification request not found");
        }
        if (!CERT_STATUS_PENDING.equals(resolveCertificationStatus(user, certification))) {
            throw new BusinessException("Only pending certification can be audited");
        }

        String targetStatus = normalizeAuditStatus(request.getStatus());
        certification.setStatus(targetStatus);
        certification.setRejectReason(CERT_STATUS_REJECTED.equals(targetStatus)
                ? requireText(request.getRejectReason(), "Reject reason is required")
                : null);
        certification.setAuditTime(LocalDateTime.now());
        certification.setAuditorId(SecurityUtil.currentUserId());
        volunteerCertificationMapper.updateById(certification);

        user.setCertified(CERT_STATUS_APPROVED.equals(targetStatus) ? 1 : 0);
        if (CERT_STATUS_APPROVED.equals(targetStatus)) {
            user.setRealName(certification.getRealName());
        }
        userMapper.updateById(user);
        adminOperationLogService.record(
                "AUDIT_CERTIFICATION",
                "CERTIFICATION",
                certification.getId(),
                user.getUsername(),
                "status=" + targetStatus
                        + (CERT_STATUS_REJECTED.equals(targetStatus) ? ", reason=" + certification.getRejectReason() : "")
        );
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        return user;
    }

    private void ensureUsernameAvailable(String username, Long excludeUserId) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .ne(excludeUserId != null, User::getId, excludeUserId));
        if (count != null && count > 0) {
            throw new BusinessException("Username already exists");
        }
    }

    private void sanitizeListUser(User user) {
        sanitizeDetailUser(user);
        user.setPhone(maskPhone(user.getPhone()));
        user.setEmail(maskEmail(user.getEmail()));
        user.setCertificationIdCardNo(maskIdCard(user.getCertificationIdCardNo()));
        user.setCertificationIdCardFrontUrl(null);
        user.setCertificationIdCardBackUrl(null);
    }

    private void sanitizeDetailUser(User user) {
        user.setPassword(null);
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }
        String normalized = phone.trim();
        if (normalized.length() <= 7) {
            return normalized.charAt(0) + "***";
        }
        return normalized.substring(0, 3) + "****" + normalized.substring(normalized.length() - 4);
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        String normalized = email.trim();
        int atIndex = normalized.indexOf('@');
        if (atIndex <= 1) {
            return "***" + normalized.substring(Math.max(atIndex, 0));
        }
        return normalized.charAt(0) + "***" + normalized.substring(atIndex);
    }

    private String maskIdCard(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String normalized = value.trim();
        if (normalized.length() <= 8) {
            return normalized.charAt(0) + "****" + normalized.charAt(normalized.length() - 1);
        }
        return normalized.substring(0, 4) + "********" + normalized.substring(normalized.length() - 4);
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeIdCardNo(String value) {
        String normalized = requireText(value, "ID card number is required").toUpperCase();
        if (!normalized.matches("(^\\d{15}$)|(^\\d{17}[\\dX]$)")) {
            throw new BusinessException("ID card number format is invalid");
        }
        return normalized;
    }

    private int normalizeBinaryValue(Integer value, Integer fallback, String message) {
        int resolved = value == null ? (fallback == null ? 0 : fallback) : value;
        if (resolved != 0 && resolved != 1) {
            throw new BusinessException(message);
        }
        return resolved;
    }

    private String normalizeRole(String role, String fallback) {
        if (!StringUtils.hasText(role)) {
            return fallback;
        }
        String normalized = role.trim().toUpperCase();
        if (!ROLE_ADMIN.equals(normalized) && !ROLE_VOLUNTEER.equals(normalized)) {
            throw new BusinessException("Role is invalid");
        }
        return normalized;
    }

    private User requireCurrentVolunteer() {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "User is not logged in");
        }
        return requireVolunteerUser(userId);
    }

    private User requireVolunteerUser(Long userId) {
        User user = requireUser(userId);
        if (!ROLE_VOLUNTEER.equals(user.getRole())) {
            throw new BusinessException(400, "Target user is not a volunteer");
        }
        return user;
    }

    private VolunteerCertification findCertificationByUserId(Long userId) {
        return volunteerCertificationMapper.selectOne(new LambdaQueryWrapper<VolunteerCertification>()
                .eq(VolunteerCertification::getUserId, userId)
                .last("limit 1"));
    }

    private VolunteerCertification buildCertificationView(User user, VolunteerCertification certification) {
        VolunteerCertification view = certification == null ? new VolunteerCertification() : certification;
        view.setUserId(user.getId());
        if (!StringUtils.hasText(view.getRealName())) {
            view.setRealName(user.getRealName());
        }
        view.setStatus(resolveCertificationStatus(user, certification));
        return view;
    }

    private String resolveCertificationStatus(User user, VolunteerCertification certification) {
        if (certification != null && StringUtils.hasText(certification.getStatus())) {
            return certification.getStatus();
        }
        return Objects.equals(user.getCertified(), 1) ? CERT_STATUS_APPROVED : CERT_STATUS_NOT_SUBMITTED;
    }

    private void enrichUsersWithCertification(List<User> users, boolean sanitizeForList) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(User::getId).filter(Objects::nonNull).distinct().toList();
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, VolunteerCertification> certificationMap = volunteerCertificationMapper.selectList(
                        new LambdaQueryWrapper<VolunteerCertification>().in(VolunteerCertification::getUserId, userIds)
                ).stream()
                .collect(Collectors.toMap(VolunteerCertification::getUserId, Function.identity(), (left, right) -> right));
        users.forEach(user -> applyCertificationSnapshot(user, certificationMap.get(user.getId()), sanitizeForList));
    }

    private void applyCertificationSnapshot(User user,
                                            VolunteerCertification certification,
                                            boolean sanitizeForList) {
        user.setCertificationId(certification == null ? null : certification.getId());
        user.setCertificationStatus(resolveCertificationStatus(user, certification));
        user.setCertificationRejectReason(certification == null ? null : certification.getRejectReason());
        user.setCertificationIdCardNo(certification == null ? null : certification.getIdCardNo());
        user.setCertificationIdCardFrontUrl(sanitizeForList || certification == null ? null : certification.getIdCardFrontUrl());
        user.setCertificationIdCardBackUrl(sanitizeForList || certification == null ? null : certification.getIdCardBackUrl());
        user.setCertificationSubmitTime(certification == null ? null : certification.getSubmitTime());
        user.setCertificationAuditTime(certification == null ? null : certification.getAuditTime());
        user.setCertificationAuditorId(certification == null ? null : certification.getAuditorId());
    }

    private String normalizeCertificationStatusFilter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (!CERT_STATUS_NOT_SUBMITTED.equals(normalized)
                && !CERT_STATUS_PENDING.equals(normalized)
                && !CERT_STATUS_APPROVED.equals(normalized)
                && !CERT_STATUS_REJECTED.equals(normalized)) {
            throw new BusinessException("Certification status is invalid");
        }
        return normalized;
    }

    private String normalizeAuditStatus(String value) {
        String normalized = normalizeCertificationStatusFilter(value);
        if (!CERT_STATUS_APPROVED.equals(normalized) && !CERT_STATUS_REJECTED.equals(normalized)) {
            throw new BusinessException("Audit status is invalid");
        }
        return normalized;
    }
}
