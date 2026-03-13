package com.community.modules.content.service;

import com.community.common.web.PageResult;
import com.community.modules.content.dto.FavoriteRequest;
import com.community.modules.content.dto.FavoriteUpdateRequest;
import com.community.modules.content.entity.BannerInfo;
import com.community.modules.content.entity.CommentInfo;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.entity.ForumCategory;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.entity.InfoDynamic;
import com.community.modules.content.entity.NoticeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;

    public PageResult<InfoDynamic> pageDynamics(long current, long size, String type, String keyword, boolean onlyPublished) {
        return contentQueryService.pageDynamics(current, size, type, keyword, onlyPublished);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveDynamic(InfoDynamic dynamic) {
        contentCommandService.saveDynamic(dynamic);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDynamic(Long id, InfoDynamic dynamic) {
        contentCommandService.updateDynamic(id, dynamic);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDynamic(Long id) {
        contentCommandService.deleteDynamic(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteDynamics(List<Long> ids) {
        contentCommandService.batchDeleteDynamics(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchArchiveDynamics(List<Long> ids) {
        contentCommandService.batchArchiveDynamics(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchRestoreDynamics(List<Long> ids) {
        contentCommandService.batchRestoreDynamics(ids);
    }

    public List<InfoDynamic> hotDynamicsTop5() {
        return contentQueryService.hotDynamicsTop5();
    }

    public PageResult<NoticeInfo> pageNotices(long current, long size, boolean onlyPublished) {
        return contentQueryService.pageNotices(current, size, onlyPublished);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveNotice(NoticeInfo notice) {
        contentCommandService.saveNotice(notice);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(Long id, NoticeInfo notice) {
        contentCommandService.updateNotice(id, notice);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        contentCommandService.deleteNotice(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteNotices(List<Long> ids) {
        contentCommandService.batchDeleteNotices(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchArchiveNotices(List<Long> ids) {
        contentCommandService.batchArchiveNotices(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchRestoreNotices(List<Long> ids) {
        contentCommandService.batchRestoreNotices(ids);
    }

    public List<BannerInfo> listBanners() {
        return contentQueryService.listBanners();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveBanner(BannerInfo banner) {
        contentCommandService.saveBanner(banner);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBanner(Long id, BannerInfo banner) {
        contentCommandService.updateBanner(id, banner);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        contentCommandService.deleteBanner(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteBanners(List<Long> ids) {
        contentCommandService.batchDeleteBanners(ids);
    }

    public List<ForumCategory> listForumCategories() {
        return contentQueryService.listForumCategories();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveForumCategory(ForumCategory category) {
        contentCommandService.saveForumCategory(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateForumCategory(Long id, ForumCategory category) {
        contentCommandService.updateForumCategory(id, category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteForumCategory(Long id) {
        contentCommandService.deleteForumCategory(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteForumCategories(List<Long> ids) {
        contentCommandService.batchDeleteForumCategories(ids);
    }

    public PageResult<ForumPost> pageForumPosts(long current, long size, String keyword, String status, boolean onlyMine, boolean onlyApproved) {
        return contentQueryService.pageForumPosts(current, size, keyword, status, onlyMine, onlyApproved);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateMyPost(ForumPost post) {
        contentCommandService.saveOrUpdateMyPost(post);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long id, String status, String reason) {
        contentCommandService.auditPost(id, status, reason);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteForumPost(Long id) {
        contentCommandService.deleteForumPost(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteForumPosts(List<Long> ids) {
        contentCommandService.batchDeleteForumPosts(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addComment(CommentInfo comment) {
        contentCommandService.addComment(comment);
    }

    public PageResult<CommentInfo> pageComments(long current,
                                                long size,
                                                boolean onlyMine,
                                                String targetType,
                                                Long targetId,
                                                boolean includeTestData) {
        return contentQueryService.pageComments(current, size, onlyMine, targetType, targetId, includeTestData);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        contentCommandService.deleteComment(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteComments(List<Long> ids) {
        contentCommandService.batchDeleteComments(ids);
    }

    public List<FavoriteActivity> myFavorites() {
        return contentQueryService.myFavorites();
    }

    public PageResult<FavoriteActivity> pageFavorites(long current, long size) {
        return contentQueryService.pageFavorites(current, size);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createFavorite(FavoriteRequest request) {
        contentCommandService.createFavorite(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFavorite(Long id, FavoriteUpdateRequest request) {
        contentCommandService.updateFavorite(id, request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFavoriteById(Long id) {
        contentCommandService.removeFavoriteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveFavoriteById(List<Long> ids) {
        contentCommandService.batchRemoveFavoriteById(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long activityId) {
        contentCommandService.addFavorite(activityId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long activityId) {
        contentCommandService.removeFavorite(activityId);
    }
}
