package com.community.modules.activity.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.activity.dto.ActivityCategoryRequest;
import com.community.modules.activity.dto.ActivityRequest;
import com.community.modules.activity.dto.ApplicationAuditRequest;
import com.community.modules.activity.dto.SignRequest;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.entity.ActivityCategory;
import com.community.modules.activity.entity.ActivityCheckRecord;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.activity.mapper.ActivityCategoryMapper;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final double MAX_DISTANCE_METERS = 500.0;

    private final ActivityCategoryMapper categoryMapper;
    private final ActivityMapper activityMapper;
    private final ActivityApplicationMapper applicationMapper;
    private final ActivityCheckRecordMapper checkRecordMapper;
    private final UserMapper userMapper;

    public List<ActivityCategory> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<ActivityCategory>()
                .orderByAsc(ActivityCategory::getSort)
                .orderByDesc(ActivityCategory::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void createCategory(ActivityCategoryRequest request) {
        ActivityCategory category = new ActivityCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        categoryMapper.insert(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, ActivityCategoryRequest request) {
        ActivityCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "Category not found");
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        categoryMapper.updateById(category);
    }

    public PageResult<Activity> pageActivities(long current, long size, String keyword, Long categoryId, String status) {
        Page<Activity> page = new Page<>(current, size);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), Activity::getTitle, keyword)
                .eq(categoryId != null, Activity::getCategoryId, categoryId)
                .eq(StringUtils.hasText(status), Activity::getStatus, status)
                .orderByDesc(Activity::getCreateTime);
        Page<Activity> result = activityMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void createActivity(ActivityRequest request) {
        validateActivityTime(request.getStartTime(), request.getEndTime());
        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setCategoryId(request.getCategoryId());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setAddress(request.getAddress());
        activity.setTargetCount(request.getTargetCount());
        activity.setDescription(request.getDescription());
        activity.setLatitude(request.getLatitude());
        activity.setLongitude(request.getLongitude());
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
        activity.setTitle(request.getTitle());
        activity.setCategoryId(request.getCategoryId());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setAddress(request.getAddress());
        activity.setTargetCount(request.getTargetCount());
        activity.setDescription(request.getDescription());
        activity.setLatitude(request.getLatitude());
        activity.setLongitude(request.getLongitude());
        activity.setStatus(StrUtil.blankToDefault(request.getStatus(), activity.getStatus()));
        activityMapper.updateById(activity);
    }

    public Activity detail(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }
        return activity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyForActivity(Long activityId) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "Unauthenticated");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        if (user.getCertified() == null || user.getCertified() != 1) {
            throw new BusinessException("Your certification is not approved yet");
        }
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }
        if ("ENDED".equalsIgnoreCase(activity.getStatus())) {
            throw new BusinessException("Activity already ended");
        }

        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<ActivityApplication>()
                .eq(ActivityApplication::getActivityId, activityId)
                .eq(ActivityApplication::getUserId, userId));
        if (count != null && count > 0) {
            throw new BusinessException("You have already applied for this activity");
        }

        ActivityApplication application = new ActivityApplication();
        application.setActivityId(activityId);
        application.setUserId(userId);
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(application);
    }

    public PageResult<ActivityApplication> pageApplications(long current, long size, Long activityId, String status, boolean onlyMine) {
        Page<ActivityApplication> page = new Page<>(current, size);
        Long currentUserId = SecurityUtil.currentUserId();
        LambdaQueryWrapper<ActivityApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(activityId != null, ActivityApplication::getActivityId, activityId)
                .eq(StringUtils.hasText(status), ActivityApplication::getStatus, status)
                .eq(onlyMine && currentUserId != null, ActivityApplication::getUserId, currentUserId)
                .orderByDesc(ActivityApplication::getApplyTime);
        Page<ActivityApplication> result = applicationMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditApplication(Long applicationId, ApplicationAuditRequest request) {
        ActivityApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "Application not found");
        }
        String status = request.getStatus().toUpperCase();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("Status must be APPROVED or REJECTED");
        }
        if ("REJECTED".equals(status) && !StringUtils.hasText(request.getRejectReason())) {
            throw new BusinessException("Reject reason is required");
        }
        application.setStatus(status);
        application.setRejectReason(request.getRejectReason());
        application.setAuditTime(LocalDateTime.now());
        application.setAuditorId(SecurityUtil.currentUserId());
        applicationMapper.updateById(application);
    }

    @Transactional(rollbackFor = Exception.class)
    public void signIn(SignRequest request) {
        ActivityApplication application = applicationMapper.selectById(request.getApplicationId());
        if (application == null) {
            throw new BusinessException(404, "Application not found");
        }
        Long currentUserId = SecurityUtil.currentUserId();
        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "Cannot sign for other users");
        }
        if (!"APPROVED".equalsIgnoreCase(application.getStatus())) {
            throw new BusinessException("Application has not been approved");
        }
        Activity activity = activityMapper.selectById(application.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime signInStart = activity.getStartTime().minusMinutes(15);
        if (now.isBefore(signInStart) || now.isAfter(activity.getStartTime())) {
            throw new BusinessException("Sign-in allowed only from 15 minutes before start to start time");
        }
        double distance = calcDistanceMeters(activity.getLatitude(), activity.getLongitude(),
                request.getLatitude(), request.getLongitude());
        if (distance > MAX_DISTANCE_METERS) {
            throw new BusinessException("Distance too far from activity location");
        }

        ActivityCheckRecord record = checkRecordMapper.selectOne(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getActivityId, activity.getId())
                .eq(ActivityCheckRecord::getUserId, currentUserId)
                .last("limit 1"));
        if (record != null && record.getSignInTime() != null) {
            throw new BusinessException("Already signed in");
        }
        if (record == null) {
            record = new ActivityCheckRecord();
            record.setActivityId(activity.getId());
            record.setUserId(currentUserId);
            record.setStatus("SIGNED_IN");
            record.setSignInTime(now);
            record.setSignInDistance(distance);
            checkRecordMapper.insert(record);
        } else {
            record.setSignInTime(now);
            record.setSignInDistance(distance);
            record.setStatus("SIGNED_IN");
            checkRecordMapper.updateById(record);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void signOut(SignRequest request) {
        ActivityApplication application = applicationMapper.selectById(request.getApplicationId());
        if (application == null) {
            throw new BusinessException(404, "Application not found");
        }
        Long currentUserId = SecurityUtil.currentUserId();
        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "Cannot sign for other users");
        }
        Activity activity = activityMapper.selectById(application.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getEndTime().minusMinutes(15))) {
            throw new BusinessException("Sign-out is allowed from 15 minutes before end time");
        }
        double distance = calcDistanceMeters(activity.getLatitude(), activity.getLongitude(),
                request.getLatitude(), request.getLongitude());
        if (distance > MAX_DISTANCE_METERS) {
            throw new BusinessException("Distance too far from activity location");
        }

        ActivityCheckRecord record = checkRecordMapper.selectOne(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getActivityId, activity.getId())
                .eq(ActivityCheckRecord::getUserId, currentUserId)
                .last("limit 1"));
        if (record == null || record.getSignInTime() == null) {
            throw new BusinessException("Please sign in first");
        }
        if (record.getSignOutTime() != null) {
            throw new BusinessException("Already signed out");
        }
        record.setSignOutTime(now);
        record.setSignOutDistance(distance);
        record.setStatus("FINISHED");
        checkRecordMapper.updateById(record);
    }

    public PageResult<ActivityCheckRecord> myCheckRecords(long current, long size) {
        Long userId = SecurityUtil.currentUserId();
        Page<ActivityCheckRecord> page = new Page<>(current, size);
        Page<ActivityCheckRecord> result = checkRecordMapper.selectPage(page, new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getUserId, userId)
                .orderByDesc(ActivityCheckRecord::getCreateTime));
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
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
            throw new BusinessException("End time must be after start time");
        }
    }

    private double calcDistanceMeters(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        final double radius = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius * c;
    }
}

