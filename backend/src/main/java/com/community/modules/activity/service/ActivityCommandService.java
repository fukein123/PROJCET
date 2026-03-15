package com.community.modules.activity.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.activity.dto.ActivityRequest;
import com.community.modules.activity.dto.ApplicationAuditRequest;
import com.community.modules.activity.dto.SignRequest;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.entity.ActivityCheckRecord;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityCommandService {

    private static final Duration APPLICATION_UNDO_WINDOW = Duration.ofSeconds(30);

    private final ActivityMapper activityMapper;
    private final ActivityApplicationMapper applicationMapper;
    private final ActivityCheckRecordMapper checkRecordMapper;
    private final UserMapper userMapper;
    private final AdminOperationLogService adminOperationLogService;

    @Transactional(rollbackFor = Exception.class)
    public void createActivity(ActivityRequest request) {
        validateActivityTime(request.getStartTime(), request.getEndTime());
        Activity activity = new Activity();
        activity.setTitle(request.getTitle().trim());
        activity.setCategoryId(request.getCategoryId());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setAddress(request.getAddress().trim());
        activity.setTargetCount(request.getTargetCount());
        activity.setVolunteerQuota(request.getVolunteerQuota());
        activity.setPointReward(request.getPointReward());
        activity.setContent(request.getContent().trim());
        activity.setDescription(normalizeOptionalText(request.getDescription()));
        activity.setCoverImage(normalizeOptionalText(request.getCoverImage()));
        activity.setStatus(StrUtil.blankToDefault(request.getStatus(), "PUBLISHED"));
        activity.setCreatorId(SecurityUtil.currentUserId());
        activityMapper.insert(activity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Long id, ActivityRequest request) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }
        validateActivityTime(request.getStartTime(), request.getEndTime());
        activity.setTitle(request.getTitle().trim());
        activity.setCategoryId(request.getCategoryId());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setAddress(request.getAddress().trim());
        activity.setTargetCount(request.getTargetCount());
        activity.setVolunteerQuota(request.getVolunteerQuota());
        activity.setPointReward(request.getPointReward());
        activity.setContent(request.getContent().trim());
        activity.setDescription(normalizeOptionalText(request.getDescription()));
        activity.setCoverImage(normalizeOptionalText(request.getCoverImage()));
        activity.setStatus(StrUtil.blankToDefault(request.getStatus(), activity.getStatus()));
        activityMapper.updateById(activity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return;
        }
        if ("ARCHIVED".equalsIgnoreCase(activity.getStatus())) {
            return;
        }
        activity.setStatus("ARCHIVED");
        activityMapper.updateById(activity);
        adminOperationLogService.record(
                "ARCHIVE_ACTIVITY",
                "ACTIVITY",
                activity.getId(),
                activity.getTitle(),
                "delete alias -> archived"
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteActivities(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::deleteActivity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchArchiveActivities(List<Long> ids) {
        batchUpdateActivitiesStatus(ids, "ARCHIVED");
        adminOperationLogService.record(
                "ARCHIVE_ACTIVITY_BATCH",
                "ACTIVITY",
                null,
                "batch",
                "ids=" + summarizeIds(ids)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchRestoreActivities(List<Long> ids) {
        batchUpdateActivitiesStatus(ids, "PUBLISHED");
    }

    @Transactional(rollbackFor = Exception.class)
    public ActivityApplication applyForActivity(Long activityId, String applyReason) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "User is not logged in");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        if (user.getCertified() == null || user.getCertified() != 1) {
            throw new BusinessException("Current account has not passed certification");
        }

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }

        String normalizedApplyReason = requireText(applyReason, "Apply reason is required");
        validateActivityApplicationStatus(activity.getStatus());

        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<ActivityApplication>()
                .eq(ActivityApplication::getActivityId, activityId)
                .eq(ActivityApplication::getUserId, userId));
        if (count != null && count > 0) {
            throw new BusinessException("You have already applied for this activity");
        }

        if (activity.getVolunteerQuota() != null && activity.getVolunteerQuota() > 0) {
            Long appliedCount = applicationMapper.selectCount(new LambdaQueryWrapper<ActivityApplication>()
                    .eq(ActivityApplication::getActivityId, activityId));
            if (appliedCount != null && appliedCount >= activity.getVolunteerQuota()) {
                throw new BusinessException("Volunteer quota has been reached");
            }
        }

        ActivityApplication application = new ActivityApplication();
        application.setActivityId(activityId);
        application.setUserId(userId);
        application.setApplyReason(normalizedApplyReason);
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(application);
        return applicationMapper.selectById(application.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void undoActivityApplication(Long applicationId) {
        ActivityApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "Application not found");
        }

        Long currentUserId = SecurityUtil.currentUserId();
        if (!Objects.equals(application.getUserId(), currentUserId)) {
            throw new BusinessException(403, "Cannot undo another user's application");
        }
        if (!"PENDING".equalsIgnoreCase(application.getStatus())) {
            throw new BusinessException("Only pending applications can be undone");
        }

        LocalDateTime applyTime = application.getCreateTime() == null ? application.getApplyTime() : application.getCreateTime();
        if (applyTime == null || applyTime.plus(APPLICATION_UNDO_WINDOW).isBefore(LocalDateTime.now())) {
            throw new BusinessException("Application undo window has expired");
        }

        applicationMapper.deleteById(applicationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditApplication(Long applicationId, ApplicationAuditRequest request) {
        ActivityApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "Application not found");
        }
        if (!"PENDING".equalsIgnoreCase(application.getStatus())) {
            throw new BusinessException("Only pending application can be audited");
        }

        String status = request.getStatus().toUpperCase();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("Audit status must be APPROVED or REJECTED");
        }
        if ("REJECTED".equals(status) && !StringUtils.hasText(request.getRejectReason())) {
            throw new BusinessException("Reject reason is required");
        }

        application.setStatus(status);
        application.setRejectReason(normalizeOptionalText(request.getRejectReason()));
        application.setAuditTime(LocalDateTime.now());
        application.setAuditorId(SecurityUtil.currentUserId());
        applicationMapper.updateById(application);

        Activity activity = activityMapper.selectById(application.getActivityId());
        adminOperationLogService.record(
                "AUDIT_APPLICATION",
                "APPLICATION",
                application.getId(),
                activity == null ? "application#" + application.getId() : activity.getTitle(),
                "status=" + status + ", volunteerId=" + application.getUserId()
                        + ("REJECTED".equals(status) ? ", reason=" + application.getRejectReason() : "")
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void signIn(SignRequest request) {
        ActivityApplication application = applicationMapper.selectById(request.getApplicationId());
        if (application == null) {
            throw new BusinessException(404, "Application not found");
        }

        Long currentUserId = SecurityUtil.currentUserId();
        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "Cannot sign in for another user");
        }
        if (!"APPROVED".equalsIgnoreCase(application.getStatus())) {
            throw new BusinessException("Only approved applications can sign in");
        }

        Activity activity = activityMapper.selectById(application.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime signInStart = activity.getStartTime().minusMinutes(15);
        if (now.isBefore(signInStart) || now.isAfter(activity.getStartTime())) {
            throw new BusinessException("Sign in is only allowed from 15 minutes before the activity starts");
        }

        ActivityCheckRecord record = checkRecordMapper.selectOne(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getActivityId, activity.getId())
                .eq(ActivityCheckRecord::getUserId, currentUserId)
                .last("limit 1"));
        if (record != null && record.getSignInTime() != null) {
            throw new BusinessException("You have already signed in");
        }

        if (record == null) {
            record = new ActivityCheckRecord();
            record.setActivityId(activity.getId());
            record.setUserId(currentUserId);
            record.setStatus("SIGNED_IN");
            record.setSignInTime(now);
            checkRecordMapper.insert(record);
            return;
        }

        record.setSignInTime(now);
        record.setStatus("SIGNED_IN");
        checkRecordMapper.updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void signOut(SignRequest request) {
        ActivityApplication application = applicationMapper.selectById(request.getApplicationId());
        if (application == null) {
            throw new BusinessException(404, "Application not found");
        }

        Long currentUserId = SecurityUtil.currentUserId();
        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "Cannot sign out for another user");
        }

        Activity activity = activityMapper.selectById(application.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getEndTime().minusMinutes(15))) {
            throw new BusinessException("Sign out is only allowed within 15 minutes before the activity ends");
        }

        ActivityCheckRecord record = checkRecordMapper.selectOne(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getActivityId, activity.getId())
                .eq(ActivityCheckRecord::getUserId, currentUserId)
                .last("limit 1"));
        if (record == null || record.getSignInTime() == null) {
            throw new BusinessException("Please sign in first");
        }
        if (record.getSignOutTime() != null) {
            throw new BusinessException("You have already signed out");
        }

        record.setSignOutTime(now);
        record.setStatus("FINISHED");
        checkRecordMapper.updateById(record);
    }

    public int endExpiredActivities() {
        List<Activity> activities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .ne(Activity::getStatus, "ENDED")
                .lt(Activity::getEndTime, LocalDateTime.now()));
        if (activities.isEmpty()) {
            return 0;
        }
        activities.forEach(activity -> {
            activity.setStatus("ENDED");
            activityMapper.updateById(activity);
        });
        return activities.size();
    }

    private void validateActivityTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new BusinessException("End time must be later than start time");
        }
    }

    private void validateActivityApplicationStatus(String status) {
        if ("PUBLISHED".equalsIgnoreCase(status)) {
            return;
        }
        if ("ENDED".equalsIgnoreCase(status)) {
            throw new BusinessException("Activity has already ended");
        }
        if ("ARCHIVED".equalsIgnoreCase(status)) {
            throw new BusinessException("Archived activity cannot accept new applications");
        }
        throw new BusinessException("Current activity status does not allow new applications");
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void batchUpdateActivitiesStatus(List<Long> ids, String status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> validIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (validIds.isEmpty()) {
            return;
        }
        activityMapper.update(null, new UpdateWrapper<Activity>()
                .in("id", validIds)
                .set("status", status));
    }

    private String summarizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        return ids.stream().filter(Objects::nonNull).distinct().limit(20).toList().toString();
    }
}
