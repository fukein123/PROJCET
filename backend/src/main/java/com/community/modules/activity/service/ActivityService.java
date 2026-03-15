package com.community.modules.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
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
import com.community.modules.activity.mapper.ActivityCategoryMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityCategoryMapper categoryMapper;
    private final ActivityMapper activityMapper;
    private final ActivityQueryService activityQueryService;
    private final ActivityCommandService activityCommandService;

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
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::deleteCategory);
    }

    public PageResult<Activity> pageActivities(long current,
                                               long size,
                                               String keyword,
                                               Long categoryId,
                                               String status,
                                               boolean includeArchived) {
        return activityQueryService.pageActivities(current, size, keyword, categoryId, status, includeArchived);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createActivity(ActivityRequest request) {
        activityCommandService.createActivity(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Long id, ActivityRequest request) {
        activityCommandService.updateActivity(id, request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long id) {
        activityCommandService.deleteActivity(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteActivities(List<Long> ids) {
        activityCommandService.batchDeleteActivities(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchArchiveActivities(List<Long> ids) {
        activityCommandService.batchArchiveActivities(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchRestoreActivities(List<Long> ids) {
        activityCommandService.batchRestoreActivities(ids);
    }

    public Activity detail(Long id) {
        return activityQueryService.detail(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ActivityApplication applyForActivity(Long activityId, String applyReason) {
        return activityCommandService.applyForActivity(activityId, applyReason);
    }

    @Transactional(rollbackFor = Exception.class)
    public void undoActivityApplication(Long applicationId) {
        activityCommandService.undoActivityApplication(applicationId);
    }

    public PageResult<ActivityApplicationView> pageApplications(long current, long size, Long activityId, String status, boolean onlyMine) {
        return activityQueryService.pageApplications(current, size, activityId, status, onlyMine);
    }

    public PageResult<CheckRecordView> pageCheckRecords(long current,
                                                        long size,
                                                        Long activityId,
                                                        Long userId,
                                                        String status,
                                                        String keyword) {
        return activityQueryService.pageCheckRecords(current, size, activityId, userId, status, keyword);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditApplication(Long applicationId, ApplicationAuditRequest request) {
        activityCommandService.auditApplication(applicationId, request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void signIn(SignRequest request) {
        activityCommandService.signIn(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void signOut(SignRequest request) {
        activityCommandService.signOut(request);
    }

    public PageResult<CheckRecordView> myCheckRecords(long current, long size) {
        return activityQueryService.myCheckRecords(current, size);
    }

    public int endExpiredActivities() {
        return activityCommandService.endExpiredActivities();
    }

}
