package com.community.modules.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.activity.dto.ActivityApplicationView;
import com.community.modules.activity.dto.CheckRecordView;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.entity.ActivityCheckRecord;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityQueryService {

    private final ActivityMapper activityMapper;
    private final ActivityApplicationMapper applicationMapper;
    private final ActivityCheckRecordMapper checkRecordMapper;
    private final UserMapper userMapper;

    public PageResult<Activity> pageActivities(long current,
                                               long size,
                                               String keyword,
                                               Long categoryId,
                                               String status,
                                               boolean includeArchived) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), Activity::getTitle, keyword)
                .eq(categoryId != null, Activity::getCategoryId, categoryId)
                .eq(StringUtils.hasText(status), Activity::getStatus, status)
                .orderByDesc(Activity::getCreateTime);
        if (!includeArchived) {
            wrapper.ne(Activity::getStatus, "ARCHIVED");
        }
        PageHelper.startPage((int) current, (int) size);
        List<Activity> records = activityMapper.selectList(wrapper);
        PageInfo<Activity> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public Activity detail(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        return activity;
    }

    public PageResult<ActivityApplicationView> pageApplications(long current, long size, Long activityId, String status, boolean onlyMine) {
        Long currentUserId = SecurityUtil.currentUserId();
        LambdaQueryWrapper<ActivityApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(activityId != null, ActivityApplication::getActivityId, activityId)
                .eq(StringUtils.hasText(status), ActivityApplication::getStatus, status)
                .eq(onlyMine && currentUserId != null, ActivityApplication::getUserId, currentUserId)
                .orderByDesc(ActivityApplication::getApplyTime);
        PageHelper.startPage((int) current, (int) size);
        List<ActivityApplication> records = applicationMapper.selectList(wrapper);
        PageInfo<ActivityApplication> pageInfo = new PageInfo<>(records);

        List<Long> activityIds = records.stream()
                .map(ActivityApplication::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Activity> activityMap = new HashMap<>();
        if (!activityIds.isEmpty()) {
            activityMapper.selectBatchIds(activityIds).forEach(item -> activityMap.put(item.getId(), item));
        }

        List<Long> userIds = records.stream()
                .map(ActivityApplication::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(item -> userMap.put(item.getId(), item));
        }

        List<ActivityApplicationView> views = records.stream().map(record -> {
            ActivityApplicationView view = new ActivityApplicationView();
            view.setId(record.getId());
            view.setActivityId(record.getActivityId());
            view.setUserId(record.getUserId());
            view.setStatus(record.getStatus());
            view.setRejectReason(record.getRejectReason());
            view.setApplyTime(record.getApplyTime());
            view.setAuditTime(record.getAuditTime());
            view.setAuditorId(record.getAuditorId());
            Activity activity = activityMap.get(record.getActivityId());
            if (activity != null) {
                view.setActivityTitle(activity.getTitle());
            }
            User user = userMap.get(record.getUserId());
            if (user != null) {
                view.setUsername(user.getUsername());
                view.setRealName(user.getRealName());
            }
            return view;
        }).toList();

        return new PageResult<>(pageInfo.getTotal(), current, size, views);
    }

    public PageResult<CheckRecordView> myCheckRecords(long current, long size) {
        Long userId = SecurityUtil.currentUserId();
        PageHelper.startPage((int) current, (int) size);
        List<ActivityCheckRecord> records = checkRecordMapper.selectList(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getUserId, userId)
                .orderByDesc(ActivityCheckRecord::getCreateTime));
        PageInfo<ActivityCheckRecord> pageInfo = new PageInfo<>(records);
        List<Long> activityIds = records.stream()
                .map(ActivityCheckRecord::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Activity> activityMap = new HashMap<>();
        if (!activityIds.isEmpty()) {
            List<Activity> activities = activityMapper.selectBatchIds(activityIds);
            activities.forEach(item -> activityMap.put(item.getId(), item));
        }

        List<CheckRecordView> views = records.stream().map(record -> {
            Activity activity = activityMap.get(record.getActivityId());
            CheckRecordView view = new CheckRecordView();
            view.setId(record.getId());
            view.setActivityId(record.getActivityId());
            view.setSignInTime(record.getSignInTime());
            view.setSignOutTime(record.getSignOutTime());
            view.setSignInDistance(record.getSignInDistance());
            view.setSignOutDistance(record.getSignOutDistance());
            view.setStatus(record.getStatus());
            if (activity != null) {
                view.setActivityTitle(activity.getTitle());
                view.setActivityAddress(activity.getAddress());
                view.setActivityStartTime(activity.getStartTime());
                view.setActivityEndTime(activity.getEndTime());
            }
            view.setServiceMinutes(calculateServiceMinutes(record.getSignInTime(), record.getSignOutTime()));
            return view;
        }).toList();

        return new PageResult<>(pageInfo.getTotal(), current, size, views);
    }

    private Long calculateServiceMinutes(LocalDateTime signInTime, LocalDateTime signOutTime) {
        if (signInTime == null || signOutTime == null || signOutTime.isBefore(signInTime)) {
            return 0L;
        }
        return ChronoUnit.MINUTES.between(signInTime, signOutTime);
    }
}
