package com.community.modules.content.controller;

import com.community.common.web.ApiResponse;
import com.community.common.web.PageResult;
import com.community.modules.content.entity.*;
import com.community.modules.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @Operation(summary = "Home data for volunteer portal")
    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("banners", contentService.listBanners());
        data.put("hotDynamics", contentService.hotDynamicsTop5());
        data.put("notices", contentService.pageNotices(1, 6, true).getRecords());
        data.put("hotPosts", contentService.pageForumPosts(1, 6, null, null, false, true).getRecords());
        return ApiResponse.success(data);
    }

    @Operation(summary = "Page dynamics")
    @GetMapping("/dynamics/page")
    public ApiResponse<PageResult<InfoDynamic>> pageDynamics(@RequestParam(defaultValue = "1") long current,
                                                             @RequestParam(defaultValue = "10") long size,
                                                             @RequestParam(required = false) String type,
                                                             @RequestParam(required = false) String keyword,
                                                             @RequestParam(defaultValue = "true") boolean onlyPublished) {
        return ApiResponse.success(contentService.pageDynamics(current, size, type, keyword, onlyPublished));
    }

    @Operation(summary = "Admin - create dynamic")
    @PostMapping("/dynamics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> saveDynamic(@RequestBody InfoDynamic dynamic) {
        contentService.saveDynamic(dynamic);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Admin - update dynamic")
    @PutMapping("/dynamics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateDynamic(@PathVariable Long id, @RequestBody InfoDynamic dynamic) {
        contentService.updateDynamic(id, dynamic);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Page notices")
    @GetMapping("/notices/page")
    public ApiResponse<PageResult<NoticeInfo>> pageNotices(@RequestParam(defaultValue = "1") long current,
                                                           @RequestParam(defaultValue = "10") long size,
                                                           @RequestParam(defaultValue = "true") boolean onlyPublished) {
        return ApiResponse.success(contentService.pageNotices(current, size, onlyPublished));
    }

    @Operation(summary = "Admin - create notice")
    @PostMapping("/notices")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> saveNotice(@RequestBody NoticeInfo notice) {
        contentService.saveNotice(notice);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Admin - update notice")
    @PutMapping("/notices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateNotice(@PathVariable Long id, @RequestBody NoticeInfo notice) {
        contentService.updateNotice(id, notice);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "List active banners")
    @GetMapping("/banners")
    public ApiResponse<List<BannerInfo>> banners() {
        return ApiResponse.success(contentService.listBanners());
    }

    @Operation(summary = "Admin - create banner")
    @PostMapping("/banners")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> saveBanner(@RequestBody BannerInfo banner) {
        contentService.saveBanner(banner);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Admin - update banner")
    @PutMapping("/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateBanner(@PathVariable Long id, @RequestBody BannerInfo banner) {
        contentService.updateBanner(id, banner);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "List forum categories")
    @GetMapping("/forum/categories")
    public ApiResponse<List<ForumCategory>> forumCategories() {
        return ApiResponse.success(contentService.listForumCategories());
    }

    @Operation(summary = "Admin - create forum category")
    @PostMapping("/forum/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> saveForumCategory(@RequestBody ForumCategory category) {
        contentService.saveForumCategory(category);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Admin - update forum category")
    @PutMapping("/forum/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateForumCategory(@PathVariable Long id, @RequestBody ForumCategory category) {
        contentService.updateForumCategory(id, category);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Page forum posts")
    @GetMapping("/forum/posts/page")
    public ApiResponse<PageResult<ForumPost>> pageForumPosts(@RequestParam(defaultValue = "1") long current,
                                                             @RequestParam(defaultValue = "10") long size,
                                                             @RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) String status,
                                                             @RequestParam(defaultValue = "false") boolean onlyMine,
                                                             @RequestParam(defaultValue = "true") boolean onlyApproved) {
        return ApiResponse.success(contentService.pageForumPosts(current, size, keyword, status, onlyMine, onlyApproved));
    }

    @Operation(summary = "Volunteer - create/update my post")
    @PostMapping("/forum/posts/my")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> saveOrUpdateMyPost(@RequestBody ForumPost post) {
        contentService.saveOrUpdateMyPost(post);
        return ApiResponse.success("saved", null);
    }

    @Operation(summary = "Admin - audit post")
    @PutMapping("/forum/posts/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> auditPost(@PathVariable Long id,
                                       @RequestParam String status,
                                       @RequestParam(required = false) String reason) {
        contentService.auditPost(id, status, reason);
        return ApiResponse.success("audited", null);
    }

    @Operation(summary = "Create comment")
    @PostMapping("/comments")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTEER')")
    public ApiResponse<Void> addComment(@RequestBody CommentInfo comment) {
        contentService.addComment(comment);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Page comments")
    @GetMapping("/comments/page")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTEER')")
    public ApiResponse<PageResult<CommentInfo>> pageComments(@RequestParam(defaultValue = "1") long current,
                                                             @RequestParam(defaultValue = "10") long size,
                                                             @RequestParam(defaultValue = "false") boolean onlyMine,
                                                             @RequestParam(required = false) String targetType) {
        return ApiResponse.success(contentService.pageComments(current, size, onlyMine, targetType));
    }

    @Operation(summary = "Page exchange orders")
    @GetMapping("/orders/page")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTEER')")
    public ApiResponse<PageResult<ExchangeOrder>> pageOrders(@RequestParam(defaultValue = "1") long current,
                                                             @RequestParam(defaultValue = "10") long size,
                                                             @RequestParam(defaultValue = "false") boolean onlyMine) {
        return ApiResponse.success(contentService.pageOrders(current, size, onlyMine));
    }

    @Operation(summary = "Create exchange order")
    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTEER')")
    public ApiResponse<Void> saveOrder(@RequestBody ExchangeOrder order) {
        contentService.saveOrder(order);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "My favorite activities")
    @GetMapping("/favorites")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<List<FavoriteActivity>> myFavorites() {
        return ApiResponse.success(contentService.myFavorites());
    }

    @Operation(summary = "Add favorite")
    @PostMapping("/favorites/{activityId}")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> addFavorite(@PathVariable Long activityId) {
        contentService.addFavorite(activityId);
        return ApiResponse.success("collected", null);
    }

    @Operation(summary = "Remove favorite")
    @DeleteMapping("/favorites/{activityId}")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> removeFavorite(@PathVariable Long activityId) {
        contentService.removeFavorite(activityId);
        return ApiResponse.success("removed", null);
    }
}

