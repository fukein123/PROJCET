package com.community.modules.activity.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.modules.activity.dto.ActivityRequest;
import com.community.modules.activity.dto.ApplicationAuditRequest;
import com.community.modules.activity.dto.SignRequest;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.entity.ActivityCheckRecord;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.mapper.FavoriteActivityMapper;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityCommandService {

    private final ActivityMapper activityMapper;
    private final ActivityApplicationMapper applicationMapper;
    private final ActivityCheckRecordMapper checkRecordMapper;
    private final FavoriteActivityMapper favoriteActivityMapper;
    private final UserMapper userMapper;

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

    @Transactional(rollbackFor = Exception.class)
    public void batchArchiveActivities(List<Long> ids) {
        batchUpdateActivitiesStatus(ids, "ARCHIVED");
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchRestoreActivities(List<Long> ids) {
        batchUpdateActivitiesStatus(ids, "PUBLISHED");
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
        if ("ARCHIVED".equalsIgnoreCase(activity.getStatus())) {
            throw new BusinessException("该活动已归档，暂不可报名");
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
}
