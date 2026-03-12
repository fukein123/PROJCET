package com.community.modules.activity.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.activity.dto.ActivityCategoryRequest;
import com.community.modules.activity.dto.ActivityRequest;
import com.community.modules.activity.dto.ActivityApplicationView;
import com.community.modules.activity.dto.ApplicationAuditRequest;
import com.community.modules.activity.dto.CheckRecordView;
import com.community.modules.activity.dto.SignRequest;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.entity.ActivityCategory;
import com.community.modules.activity.entity.ActivityCheckRecord;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.activity.mapper.ActivityCategoryMapper;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.mapper.FavoriteActivityMapper;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityCategoryMapper categoryMapper;
    private final ActivityMapper activityMapper;
    private final ActivityApplicationMapper applicationMapper;
    private final ActivityCheckRecordMapper checkRecordMapper;
    private final FavoriteActivityMapper favoriteActivityMapper;
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
            throw new BusinessException(404, "活动分类不存在");
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        categoryMapper.updateById(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Long count = activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getCategoryId, id));
        if (count != null && count > 0) {
            throw new BusinessException("该分类下仍有活动，请先删除相关活动");
        }
        categoryMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).forEach(this::deleteCategory);
    }

    public PageResult<Activity> pageActivities(long current, long size, String keyword, Long categoryId, String status) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), Activity::getTitle, keyword)
                .eq(categoryId != null, Activity::getCategoryId, categoryId)
                .eq(StringUtils.hasText(status), Activity::getStatus, status)
                .orderByDesc(Activity::getCreateTime);
        PageHelper.startPage((int) current, (int) size);
        List<Activity> records = activityMapper.selectList(wrapper);
        PageInfo<Activity> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
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
        activity.setVolunteerQuota(request.getVolunteerQuota());
        activity.setContent(request.getContent());
        activity.setDescription(request.getDescription());
        activity.setCoverImage(request.getCoverImage());
        activity.setStatus(StrUtil.blankToDefault(request.getStatus(), "PUBLISHED"));
        activity.setCreatorId(SecurityUtil.currentUserId());
        activityMapper.insert(activity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Long id, ActivityRequest request) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        validateActivityTime(request.getStartTime(), request.getEndTime());
        activity.setTitle(request.getTitle());
        activity.setCategoryId(request.getCategoryId());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setAddress(request.getAddress());
        activity.setTargetCount(request.getTargetCount());
        activity.setVolunteerQuota(request.getVolunteerQuota());
        activity.setContent(request.getContent());
        activity.setDescription(request.getDescription());
        activity.setCoverImage(request.getCoverImage());
        activity.setStatus(StrUtil.blankToDefault(request.getStatus(), activity.getStatus()));
        activityMapper.updateById(activity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return;
        }
        applicationMapper.delete(new LambdaQueryWrapper<ActivityApplication>()
                .eq(ActivityApplication::getActivityId, id));
        checkRecordMapper.delete(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getActivityId, id));
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getActivityId, id));
        activityMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteActivities(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::deleteActivity);
    }

    public Activity detail(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        return activity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyForActivity(Long activityId) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getCertified() == null || user.getCertified() != 1) {
            throw new BusinessException("当前账号尚未通过实名认证，暂不可报名");
        }
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if ("ENDED".equalsIgnoreCase(activity.getStatus())) {
            throw new BusinessException("该活动已结束，无法报名");
        }

        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<ActivityApplication>()
                .eq(ActivityApplication::getActivityId, activityId)
                .eq(ActivityApplication::getUserId, userId));
        if (count != null && count > 0) {
            throw new BusinessException("你已报名该活动，请勿重复提交");
        }

        if (activity.getVolunteerQuota() != null && activity.getVolunteerQuota() > 0) {
            Long appliedCount = applicationMapper.selectCount(new LambdaQueryWrapper<ActivityApplication>()
                    .eq(ActivityApplication::getActivityId, activityId));
            if (appliedCount != null && appliedCount >= activity.getVolunteerQuota()) {
                throw new BusinessException("该活动报名人数已满");
            }
        }

        ActivityApplication application = new ActivityApplication();
        application.setActivityId(activityId);
        application.setUserId(userId);
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(application);
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

    @Transactional(rollbackFor = Exception.class)
    public void auditApplication(Long applicationId, ApplicationAuditRequest request) {
        ActivityApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "报名申请不存在");
        }
        String status = request.getStatus().toUpperCase();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("审核状态只能是 APPROVED 或 REJECTED");
        }
        if ("REJECTED".equals(status) && !StringUtils.hasText(request.getRejectReason())) {
            throw new BusinessException("拒绝时必须填写拒绝理由");
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
            throw new BusinessException(404, "报名申请不存在");
        }
        Long currentUserId = SecurityUtil.currentUserId();
        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "不能代替他人签到");
        }
        if (!"APPROVED".equalsIgnoreCase(application.getStatus())) {
            throw new BusinessException("该报名尚未通过审核，无法签到");
        }
        Activity activity = activityMapper.selectById(application.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime signInStart = activity.getStartTime().minusMinutes(15);
        if (now.isBefore(signInStart) || now.isAfter(activity.getStartTime())) {
            throw new BusinessException("仅支持在活动开始前 15 分钟至开始时段内签到");
        }

        ActivityCheckRecord record = checkRecordMapper.selectOne(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getActivityId, activity.getId())
                .eq(ActivityCheckRecord::getUserId, currentUserId)
                .last("limit 1"));
        if (record != null && record.getSignInTime() != null) {
            throw new BusinessException("你已完成签到，请勿重复操作");
        }
        if (record == null) {
            record = new ActivityCheckRecord();
            record.setActivityId(activity.getId());
            record.setUserId(currentUserId);
            record.setStatus("SIGNED_IN");
            record.setSignInTime(now);
            checkRecordMapper.insert(record);
        } else {
            record.setSignInTime(now);
            record.setStatus("SIGNED_IN");
            checkRecordMapper.updateById(record);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void signOut(SignRequest request) {
        ActivityApplication application = applicationMapper.selectById(request.getApplicationId());
        if (application == null) {
            throw new BusinessException(404, "报名申请不存在");
        }
        Long currentUserId = SecurityUtil.currentUserId();
        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "不能代替他人签退");
        }
        Activity activity = activityMapper.selectById(application.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getEndTime().minusMinutes(15))) {
            throw new BusinessException("仅支持在活动结束前 15 分钟内签退");
        }

        ActivityCheckRecord record = checkRecordMapper.selectOne(new LambdaQueryWrapper<ActivityCheckRecord>()
                .eq(ActivityCheckRecord::getActivityId, activity.getId())
                .eq(ActivityCheckRecord::getUserId, currentUserId)
                .last("limit 1"));
        if (record == null || record.getSignInTime() == null) {
            throw new BusinessException("请先完成签到后再签退");
        }
        if (record.getSignOutTime() != null) {
            throw new BusinessException("你已完成签退，请勿重复操作");
        }
        record.setSignOutTime(now);
        record.setStatus("FINISHED");
        checkRecordMapper.updateById(record);
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
            throw new BusinessException("结束时间必须晚于开始时间");
        }
    }

    private Long calculateServiceMinutes(LocalDateTime signInTime, LocalDateTime signOutTime) {
        if (signInTime == null || signOutTime == null || signOutTime.isBefore(signInTime)) {
            return 0L;
        }
        return ChronoUnit.MINUTES.between(signInTime, signOutTime);
    }

}
