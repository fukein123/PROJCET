package com.community.modules.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.content.entity.*;
import com.community.modules.content.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final InfoDynamicMapper infoDynamicMapper;
    private final NoticeInfoMapper noticeInfoMapper;
    private final BannerInfoMapper bannerInfoMapper;
    private final ForumCategoryMapper forumCategoryMapper;
    private final ForumPostMapper forumPostMapper;
    private final CommentInfoMapper commentInfoMapper;
    private final ExchangeOrderMapper exchangeOrderMapper;
    private final FavoriteActivityMapper favoriteActivityMapper;

    public PageResult<InfoDynamic> pageDynamics(long current, long size, String type, String keyword, boolean onlyPublished) {
        Page<InfoDynamic> page = new Page<>(current, size);
        LambdaQueryWrapper<InfoDynamic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(type), InfoDynamic::getType, type)
                .like(StringUtils.hasText(keyword), InfoDynamic::getTitle, keyword)
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
            throw new BusinessException(404, "Dynamic not found");
        }
        db.setTitle(dynamic.getTitle());
        db.setContent(dynamic.getContent());
        db.setType(dynamic.getType());
        db.setStatus(dynamic.getStatus());
        db.setPublishTime(dynamic.getPublishTime() == null ? db.getPublishTime() : dynamic.getPublishTime());
        infoDynamicMapper.updateById(db);
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
            throw new BusinessException(404, "Notice not found");
        }
        db.setTitle(notice.getTitle());
        db.setContent(notice.getContent());
        db.setStatus(notice.getStatus());
        db.setPublishTime(notice.getPublishTime() == null ? db.getPublishTime() : notice.getPublishTime());
        noticeInfoMapper.updateById(db);
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
            throw new BusinessException(404, "Banner not found");
        }
        db.setTitle(banner.getTitle());
        db.setImageUrl(banner.getImageUrl());
        db.setActivityId(banner.getActivityId());
        db.setSort(banner.getSort());
        db.setStatus(banner.getStatus());
        bannerInfoMapper.updateById(db);
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
            throw new BusinessException(404, "Forum category not found");
        }
        db.setName(category.getName());
        db.setSort(category.getSort());
        db.setStatus(category.getStatus());
        forumCategoryMapper.updateById(db);
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
            throw new BusinessException(404, "Post not found");
        }
        if (!db.getUserId().equals(userId)) {
            throw new BusinessException(403, "Cannot edit others' posts");
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
            throw new BusinessException(404, "Post not found");
        }
        if (!"APPROVED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
            throw new BusinessException("Status must be APPROVED or REJECTED");
        }
        post.setStatus(status.toUpperCase());
        post.setAuditReason(reason);
        forumPostMapper.updateById(post);
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
                .orderByDesc(CommentInfo::getCreateTime);
        Page<CommentInfo> result = commentInfoMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public PageResult<ExchangeOrder> pageOrders(long current, long size, boolean onlyMine) {
        Page<ExchangeOrder> page = new Page<>(current, size);
        Long userId = SecurityUtil.currentUserId();
        LambdaQueryWrapper<ExchangeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(onlyMine && userId != null, ExchangeOrder::getUserId, userId)
                .orderByDesc(ExchangeOrder::getCreateTime);
        Page<ExchangeOrder> result = exchangeOrderMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(ExchangeOrder order) {
        Long currentUserId = SecurityUtil.currentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "User not authenticated");
        }
        String role = SecurityUtil.currentRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            order.setUserId(order.getUserId() == null ? currentUserId : order.getUserId());
        } else {
            order.setUserId(currentUserId);
        }
        order.setStatus(order.getStatus() == null ? "PENDING" : order.getStatus());
        exchangeOrderMapper.insert(order);
    }

    public List<FavoriteActivity> myFavorites() {
        return favoriteActivityMapper.selectList(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .orderByDesc(FavoriteActivity::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long activityId) {
        Long userId = SecurityUtil.currentUserId();
        Long count = favoriteActivityMapper.selectCount(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, userId)
                .eq(FavoriteActivity::getActivityId, activityId));
        if (count != null && count > 0) {
            return;
        }
        FavoriteActivity favorite = new FavoriteActivity();
        favorite.setUserId(userId);
        favorite.setActivityId(activityId);
        favoriteActivityMapper.insert(favorite);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long activityId) {
        favoriteActivityMapper.delete(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .eq(FavoriteActivity::getActivityId, activityId));
    }
}
