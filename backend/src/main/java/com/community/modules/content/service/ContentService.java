package com.community.modules.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.content.dto.FavoriteRequest;
import com.community.modules.content.dto.FavoriteUpdateRequest;
import com.community.modules.content.entity.BannerInfo;
import com.community.modules.content.entity.CommentInfo;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.entity.ForumCategory;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.entity.InfoDynamic;
import com.community.modules.content.entity.NoticeInfo;
import com.community.modules.content.mapper.BannerInfoMapper;
import com.community.modules.content.mapper.CommentInfoMapper;
import com.community.modules.content.mapper.FavoriteActivityMapper;
import com.community.modules.content.mapper.ForumCategoryMapper;
import com.community.modules.content.mapper.ForumPostMapper;
import com.community.modules.content.mapper.InfoDynamicMapper;
import com.community.modules.content.mapper.NoticeInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final InfoDynamicMapper infoDynamicMapper;
    private final NoticeInfoMapper noticeInfoMapper;
    private final BannerInfoMapper bannerInfoMapper;
    private final ForumCategoryMapper forumCategoryMapper;
    private final ForumPostMapper forumPostMapper;
    private final CommentInfoMapper commentInfoMapper;
    private final FavoriteActivityMapper favoriteActivityMapper;
    private final ActivityMapper activityMapper;

    public PageResult<InfoDynamic> pageDynamics(long current, long size, String type, String keyword, boolean onlyPublished) {
        Page<InfoDynamic> page = new Page<>(current, size);
        LambdaQueryWrapper<InfoDynamic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(type), InfoDynamic::getType, type)
                .and(StringUtils.hasText(keyword), q -> q.like(InfoDynamic::getTitle, keyword)
                        .or().like(InfoDynamic::getContent, keyword))
                .eq(onlyPublished, InfoDynamic::getStatus, 1)
                .orderByDesc(InfoDynamic::getPublishTime);
        Page<InfoDynamic> result = infoDynamicMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveDynamic(InfoDynamic dynamic) {
        dynamic.setViews(dynamic.getViews() == null ? 0 : dynamic.getViews());
        dynamic.setStatus(dynamic.getStatus() == null ? 1 : dynamic.getStatus());
        dynamic.setPublishTime(dynamic.getPublishTime() == null ? LocalDateTime.now() : dynamic.getPublishTime());
        dynamic.setAuthorId(dynamic.getAuthorId() == null ? SecurityUtil.currentUserId() : dynamic.getAuthorId());
        infoDynamicMapper.insert(dynamic);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDynamic(Long id, InfoDynamic dynamic) {
        InfoDynamic db = infoDynamicMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "资讯动态不存在");
        }
        db.setTitle(dynamic.getTitle());
        db.setContent(dynamic.getContent());
        db.setImageUrl(dynamic.getImageUrl());
        db.setType(dynamic.getType());
        db.setStatus(dynamic.getStatus());
        db.setPublishTime(dynamic.getPublishTime() == null ? db.getPublishTime() : dynamic.getPublishTime());
        infoDynamicMapper.updateById(db);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDynamic(Long id) {
        infoDynamicMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteDynamics(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        infoDynamicMapper.deleteByIds(ids);
    }

    public List<InfoDynamic> hotDynamicsTop5() {
        return infoDynamicMapper.selectList(new LambdaQueryWrapper<InfoDynamic>()
                .eq(InfoDynamic::getStatus, 1)
                .orderByDesc(InfoDynamic::getViews)
                .last("limit 5"));
    }

    public PageResult<NoticeInfo> pageNotices(long current, long size, boolean onlyPublished) {
        Page<NoticeInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<NoticeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(onlyPublished, NoticeInfo::getStatus, 1)
                .orderByDesc(NoticeInfo::getPublishTime);
        Page<NoticeInfo> result = noticeInfoMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveNotice(NoticeInfo notice) {
        notice.setStatus(notice.getStatus() == null ? 1 : notice.getStatus());
        notice.setPublishTime(notice.getPublishTime() == null ? LocalDateTime.now() : notice.getPublishTime());
        noticeInfoMapper.insert(notice);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(Long id, NoticeInfo notice) {
        NoticeInfo db = noticeInfoMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "公告不存在");
        }
        db.setTitle(notice.getTitle());
        db.setContent(notice.getContent());
        db.setStatus(notice.getStatus());
        db.setPublishTime(notice.getPublishTime() == null ? db.getPublishTime() : notice.getPublishTime());
        noticeInfoMapper.updateById(db);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        noticeInfoMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteNotices(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        noticeInfoMapper.deleteByIds(ids);
    }

    public List<BannerInfo> listBanners() {
        return bannerInfoMapper.selectList(new LambdaQueryWrapper<BannerInfo>()
                .eq(BannerInfo::getStatus, 1)
                .orderByAsc(BannerInfo::getSort)
                .orderByDesc(BannerInfo::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveBanner(BannerInfo banner) {
        banner.setStatus(banner.getStatus() == null ? 1 : banner.getStatus());
        banner.setSort(banner.getSort() == null ? 0 : banner.getSort());
        bannerInfoMapper.insert(banner);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBanner(Long id, BannerInfo banner) {
        BannerInfo db = bannerInfoMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        db.setTitle(banner.getTitle());
        db.setImageUrl(banner.getImageUrl());
        db.setActivityId(banner.getActivityId());
        db.setSort(banner.getSort());
        db.setStatus(banner.getStatus());
        bannerInfoMapper.updateById(db);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        bannerInfoMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteBanners(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        bannerInfoMapper.deleteByIds(ids);
    }

    public List<ForumCategory> listForumCategories() {
        return forumCategoryMapper.selectList(new LambdaQueryWrapper<ForumCategory>()
                .eq(ForumCategory::getStatus, 1)
                .orderByAsc(ForumCategory::getSort)
                .orderByDesc(ForumCategory::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveForumCategory(ForumCategory category) {
        category.setStatus(category.getStatus() == null ? 1 : category.getStatus());
        category.setSort(category.getSort() == null ? 0 : category.getSort());
        forumCategoryMapper.insert(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateForumCategory(Long id, ForumCategory category) {
        ForumCategory db = forumCategoryMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "论坛分类不存在");
        }
        db.setName(category.getName());
        db.setSort(category.getSort());
        db.setStatus(category.getStatus());
        forumCategoryMapper.updateById(db);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteForumCategory(Long id) {
        List<ForumPost> posts = forumPostMapper.selectList(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getCategoryId, id));
        List<Long> postIds = posts.stream().map(ForumPost::getId).filter(Objects::nonNull).toList();
        if (!postIds.isEmpty()) {
            commentInfoMapper.delete(new LambdaQueryWrapper<CommentInfo>()
                    .eq(CommentInfo::getTargetType, "POST")
                    .in(CommentInfo::getTargetId, postIds));
            forumPostMapper.deleteByIds(postIds);
        }
        forumCategoryMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteForumCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).forEach(this::deleteForumCategory);
    }

    public PageResult<ForumPost> pageForumPosts(long current, long size, String keyword, String status, boolean onlyMine, boolean onlyApproved) {
        Page<ForumPost> page = new Page<>(current, size);
        Long userId = SecurityUtil.currentUserId();
        String role = SecurityUtil.currentRole();
        boolean mineAndAuthenticated = onlyMine && userId != null;
        boolean allowQueryNonApproved = "ADMIN".equalsIgnoreCase(role) || mineAndAuthenticated;
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), ForumPost::getTitle, keyword)
                .eq(mineAndAuthenticated, ForumPost::getUserId, userId)
                .orderByDesc(ForumPost::getCreateTime);

        if (allowQueryNonApproved) {
            if (StringUtils.hasText(status)) {
                wrapper.eq(ForumPost::getStatus, status);
            } else if (onlyApproved) {
                wrapper.eq(ForumPost::getStatus, "APPROVED");
            }
        } else {
            wrapper.eq(ForumPost::getStatus, "APPROVED");
        }

        Page<ForumPost> result = forumPostMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateMyPost(ForumPost post) {
        Long userId = SecurityUtil.currentUserId();
        if (post.getId() == null) {
            post.setUserId(userId);
            post.setStatus("PENDING");
            post.setViews(post.getViews() == null ? 0 : post.getViews());
            forumPostMapper.insert(post);
            return;
        }
        ForumPost db = forumPostMapper.selectById(post.getId());
        if (db == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!db.getUserId().equals(userId)) {
            throw new BusinessException(403, "不能编辑他人的帖子");
        }
        db.setTitle(post.getTitle());
        db.setContent(post.getContent());
        db.setCategoryId(post.getCategoryId());
        db.setStatus("PENDING");
        db.setAuditReason(null);
        forumPostMapper.updateById(db);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long id, String status, String reason) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!"APPROVED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
            throw new BusinessException("审核状态只能是 APPROVED 或 REJECTED");
        }
        post.setStatus(status.toUpperCase());
        post.setAuditReason(reason);
        forumPostMapper.updateById(post);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteForumPost(Long id) {
        forumPostMapper.deleteById(id);
        commentInfoMapper.delete(new LambdaQueryWrapper<CommentInfo>()
                .eq(CommentInfo::getTargetType, "POST")
                .eq(CommentInfo::getTargetId, id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteForumPosts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).forEach(this::deleteForumPost);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addComment(CommentInfo comment) {
        comment.setUserId(SecurityUtil.currentUserId());
        comment.setStatus(comment.getStatus() == null ? 1 : comment.getStatus());
        commentInfoMapper.insert(comment);
    }

    public PageResult<CommentInfo> pageComments(long current, long size, boolean onlyMine, String targetType) {
        Page<CommentInfo> page = new Page<>(current, size);
        Long userId = SecurityUtil.currentUserId();
        LambdaQueryWrapper<CommentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(onlyMine && userId != null, CommentInfo::getUserId, userId)
                .eq(StringUtils.hasText(targetType), CommentInfo::getTargetType, targetType)
                .eq(CommentInfo::getStatus, 1)
                .notLike(CommentInfo::getContent, "自动化冒烟评论")
                .orderByDesc(CommentInfo::getCreateTime);
        Page<CommentInfo> result = commentInfoMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        commentInfoMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteComments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        commentInfoMapper.deleteByIds(ids);
    }

    public List<FavoriteActivity> myFavorites() {
        return favoriteActivityMapper.selectList(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .orderByDesc(FavoriteActivity::getPriority)
                .orderByDesc(FavoriteActivity::getCreateTime));
    }

    public PageResult<FavoriteActivity> pageFavorites(long current, long size) {
        Page<FavoriteActivity> page = new Page<>(current, size);
        Page<FavoriteActivity> result = favoriteActivityMapper.selectPage(page,
                new LambdaQueryWrapper<FavoriteActivity>()
                        .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                        .orderByDesc(FavoriteActivity::getPriority)
                        .orderByDesc(FavoriteActivity::getCreateTime));
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void createFavorite(FavoriteRequest request) {
        Long userId = SecurityUtil.currentUserId();
        assertActivityExists(request.getActivityId());
        FavoriteActivity existing = favoriteActivityMapper.selectOne(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, userId)
                .eq(FavoriteActivity::getActivityId, request.getActivityId())
                .last("limit 1"));
        if (existing != null) {
            existing.setNote(request.getNote());
            existing.setTag(request.getTag());
            existing.setPriority(defaultPriority(request.getPriority()));
            favoriteActivityMapper.updateById(existing);
            return;
        }
        FavoriteActivity favorite = new FavoriteActivity();
        favorite.setUserId(userId);
        favorite.setActivityId(request.getActivityId());
        favorite.setNote(request.getNote());
        favorite.setTag(request.getTag());
        favorite.setPriority(defaultPriority(request.getPriority()));
        favoriteActivityMapper.insert(favorite);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFavorite(Long id, FavoriteUpdateRequest request) {
        FavoriteActivity favorite = favoriteActivityMapper.selectById(id);
        if (favorite == null || !favorite.getUserId().equals(SecurityUtil.currentUserId())) {
            throw new BusinessException(404, "收藏记录不存在");
        }
        favorite.setNote(request.getNote());
        favorite.setTag(request.getTag());
        if (request.getPriority() != null) {
            favorite.setPriority(defaultPriority(request.getPriority()));
        }
        favoriteActivityMapper.updateById(favorite);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFavoriteById(Long id) {
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getId, id)
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveFavoriteById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .in(FavoriteActivity::getId, ids));
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long activityId) {
        FavoriteRequest request = new FavoriteRequest();
        request.setActivityId(activityId);
        request.setPriority(0);
        createFavorite(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long activityId) {
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .eq(FavoriteActivity::getActivityId, activityId));
    }

    private void assertActivityExists(Long activityId) {
        if (activityId == null) {
            throw new BusinessException(400, "请先选择活动");
        }
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
    }

    private int defaultPriority(Integer priority) {
        if (priority == null) {
            return 0;
        }
        return Math.max(0, Math.min(priority, 5));
    }
}
