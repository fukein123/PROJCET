package com.community.modules.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContentCommandService {

    private final InfoDynamicMapper infoDynamicMapper;
    private final NoticeInfoMapper noticeInfoMapper;
    private final BannerInfoMapper bannerInfoMapper;
    private final ForumCategoryMapper forumCategoryMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumModerationService forumModerationService;
    private final CommentInfoMapper commentInfoMapper;
    private final FavoriteActivityMapper favoriteActivityMapper;
    private final ActivityMapper activityMapper;

    public void saveDynamic(InfoDynamic dynamic) {
        dynamic.setTitle(requireText(dynamic.getTitle(), "资讯动态标题不能为空"));
        dynamic.setContent(requireText(dynamic.getContent(), "资讯动态内容不能为空"));
        dynamic.setViews(dynamic.getViews() == null ? 0 : dynamic.getViews());
        dynamic.setStatus(dynamic.getStatus() == null ? 1 : dynamic.getStatus());
        dynamic.setPublishTime(dynamic.getPublishTime() == null ? LocalDateTime.now() : dynamic.getPublishTime());
        dynamic.setAuthorId(dynamic.getAuthorId() == null ? SecurityUtil.currentUserId() : dynamic.getAuthorId());
        infoDynamicMapper.insert(dynamic);
    }

    public void updateDynamic(Long id, InfoDynamic dynamic) {
        InfoDynamic db = infoDynamicMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "资讯动态不存在");
        }
        db.setTitle(requireText(dynamic.getTitle(), "资讯动态标题不能为空"));
        db.setContent(requireText(dynamic.getContent(), "资讯动态内容不能为空"));
        db.setImageUrl(dynamic.getImageUrl());
        db.setType(dynamic.getType());
        db.setStatus(dynamic.getStatus());
        db.setPublishTime(dynamic.getPublishTime() == null ? db.getPublishTime() : dynamic.getPublishTime());
        infoDynamicMapper.updateById(db);
    }

    public void deleteDynamic(Long id) {
        infoDynamicMapper.deleteById(id);
    }

    public void batchDeleteDynamics(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        infoDynamicMapper.deleteByIds(ids);
    }

    public void batchArchiveDynamics(List<Long> ids) {
        batchUpdateDynamicStatus(ids, 0);
    }

    public void batchRestoreDynamics(List<Long> ids) {
        batchUpdateDynamicStatus(ids, 1);
    }

    public void saveNotice(NoticeInfo notice) {
        notice.setTitle(requireText(notice.getTitle(), "公告标题不能为空"));
        notice.setContent(requireText(notice.getContent(), "公告内容不能为空"));
        notice.setStatus(notice.getStatus() == null ? 1 : notice.getStatus());
        notice.setPublishTime(notice.getPublishTime() == null ? LocalDateTime.now() : notice.getPublishTime());
        noticeInfoMapper.insert(notice);
    }

    public void updateNotice(Long id, NoticeInfo notice) {
        NoticeInfo db = noticeInfoMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "公告不存在");
        }
        db.setTitle(requireText(notice.getTitle(), "公告标题不能为空"));
        db.setContent(requireText(notice.getContent(), "公告内容不能为空"));
        db.setStatus(notice.getStatus());
        db.setPublishTime(notice.getPublishTime() == null ? db.getPublishTime() : notice.getPublishTime());
        noticeInfoMapper.updateById(db);
    }

    public void deleteNotice(Long id) {
        noticeInfoMapper.deleteById(id);
    }

    public void batchDeleteNotices(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        noticeInfoMapper.deleteByIds(ids);
    }

    public void batchArchiveNotices(List<Long> ids) {
        batchUpdateNoticeStatus(ids, 0);
    }

    public void batchRestoreNotices(List<Long> ids) {
        batchUpdateNoticeStatus(ids, 1);
    }

    public void saveBanner(BannerInfo banner) {
        banner.setStatus(banner.getStatus() == null ? 1 : banner.getStatus());
        banner.setSort(banner.getSort() == null ? 0 : banner.getSort());
        bannerInfoMapper.insert(banner);
    }

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

    public void deleteBanner(Long id) {
        bannerInfoMapper.deleteById(id);
    }

    public void batchDeleteBanners(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        bannerInfoMapper.deleteByIds(ids);
    }

    public void saveForumCategory(ForumCategory category) {
        category.setStatus(category.getStatus() == null ? 1 : category.getStatus());
        category.setSort(category.getSort() == null ? 0 : category.getSort());
        forumCategoryMapper.insert(category);
    }

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

    public void batchDeleteForumCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).forEach(this::deleteForumCategory);
    }

    public void saveOrUpdateMyPost(ForumPost post) {
        Long userId = SecurityUtil.currentUserId();
        post.setTitle(requireText(post.getTitle(), "帖子标题不能为空"));
        post.setContent(requireText(post.getContent(), "帖子内容不能为空"));
        if (post.getCategoryId() == null) {
            throw new BusinessException(400, "帖子分类不能为空");
        }
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

    public void auditPost(Long id, String status, String reason) {
        forumModerationService.auditPost(id, status, reason);
    }

    public void deleteForumPost(Long id) {
        forumPostMapper.deleteById(id);
        commentInfoMapper.delete(new LambdaQueryWrapper<CommentInfo>()
                .eq(CommentInfo::getTargetType, "POST")
                .eq(CommentInfo::getTargetId, id));
    }

    public void batchDeleteForumPosts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).forEach(this::deleteForumPost);
    }

    public void addComment(CommentInfo comment) {
        if (!StringUtils.hasText(comment.getContent())) {
            throw new BusinessException("评论内容不能为空");
        }
        assertCommentTarget(comment.getTargetType(), comment.getTargetId());
        comment.setUserId(SecurityUtil.currentUserId());
        comment.setContent(comment.getContent().trim());
        comment.setStatus(comment.getStatus() == null ? 1 : comment.getStatus());
        commentInfoMapper.insert(comment);
    }

    public void deleteComment(Long id) {
        commentInfoMapper.deleteById(id);
    }

    public void batchDeleteComments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        commentInfoMapper.deleteByIds(ids);
    }

    public void createFavorite(FavoriteRequest request) {
        Long userId = SecurityUtil.currentUserId();
        Activity activity = requireActivity(request.getActivityId());
        FavoriteActivity existing = favoriteActivityMapper.selectOne(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, userId)
                .eq(FavoriteActivity::getActivityId, request.getActivityId())
                .last("limit 1"));
        if (existing != null) {
            existing.setNote(request.getNote());
            existing.setTag(request.getTag());
            existing.setPriority(defaultPriority(request.getPriority()));
            applyActivitySnapshot(existing, activity, false);
            favoriteActivityMapper.updateById(existing);
            return;
        }
        FavoriteActivity favorite = new FavoriteActivity();
        favorite.setUserId(userId);
        favorite.setActivityId(request.getActivityId());
        favorite.setNote(request.getNote());
        favorite.setTag(request.getTag());
        favorite.setPriority(defaultPriority(request.getPriority()));
        applyActivitySnapshot(favorite, activity, true);
        favoriteActivityMapper.insert(favorite);
    }

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
        Activity activity = activityMapper.selectById(favorite.getActivityId());
        if (activity != null) {
            applyActivitySnapshot(favorite, activity, false);
        }
        favoriteActivityMapper.updateById(favorite);
    }

    public void removeFavoriteById(Long id) {
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getId, id)
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId()));
    }

    public void batchRemoveFavoriteById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .in(FavoriteActivity::getId, ids));
    }

    public void addFavorite(Long activityId) {
        FavoriteRequest request = new FavoriteRequest();
        request.setActivityId(activityId);
        request.setPriority(0);
        createFavorite(request);
    }

    public void removeFavorite(Long activityId) {
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .eq(FavoriteActivity::getActivityId, activityId));
    }

    private Activity requireActivity(Long activityId) {
        if (activityId == null) {
            throw new BusinessException(400, "请先选择活动");
        }
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        return activity;
    }

    private void assertCommentTarget(String targetType, Long targetId) {
        if (!StringUtils.hasText(targetType) || targetId == null) {
            throw new BusinessException(400, "评论目标不完整");
        }
        if ("ACTIVITY".equalsIgnoreCase(targetType)) {
            requireActivity(targetId);
            return;
        }
        if ("POST".equalsIgnoreCase(targetType)) {
            ForumPost post = forumPostMapper.selectById(targetId);
            if (post == null) {
                throw new BusinessException(404, "帖子不存在");
            }
            return;
        }
        throw new BusinessException("不支持的评论目标类型");
    }

    private int defaultPriority(Integer priority) {
        if (priority == null) {
            return 0;
        }
        return Math.max(0, Math.min(priority, 5));
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, message);
        }
        return value.trim();
    }

    private void batchUpdateDynamicStatus(List<Long> ids, int status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> validIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (validIds.isEmpty()) {
            return;
        }
        infoDynamicMapper.update(null, new UpdateWrapper<InfoDynamic>()
                .in("id", validIds)
                .set("status", status));
    }

    private void batchUpdateNoticeStatus(List<Long> ids, int status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> validIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (validIds.isEmpty()) {
            return;
        }
        noticeInfoMapper.update(null, new UpdateWrapper<NoticeInfo>()
                .in("id", validIds)
                .set("status", status));
    }

    private void applyActivitySnapshot(FavoriteActivity favorite, Activity activity, boolean overwrite) {
        if (overwrite || !StringUtils.hasText(favorite.getActivityTitle())) {
            favorite.setActivityTitle(activity.getTitle());
        }
        if (overwrite || !StringUtils.hasText(favorite.getActivityAddress())) {
            favorite.setActivityAddress(activity.getAddress());
        }
        if (overwrite || favorite.getActivityStartTime() == null) {
            favorite.setActivityStartTime(activity.getStartTime());
        }
        if (overwrite || favorite.getActivityEndTime() == null) {
            favorite.setActivityEndTime(activity.getEndTime());
        }
    }
}
