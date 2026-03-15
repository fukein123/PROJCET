package com.community.modules.content.controller;

import com.community.common.dto.IdListRequest;
import com.community.common.exception.BusinessException;
import com.community.common.web.ApiResponse;
import com.community.common.web.PageResult;
import com.community.modules.content.dto.FavoriteRequest;
import com.community.modules.content.dto.FavoriteUpdateRequest;
import com.community.modules.content.dto.ExchangeOrderCreateRequest;
import com.community.modules.content.dto.ExchangeOrderStatusRequest;
import com.community.modules.content.dto.MallProductRequest;
import com.community.modules.content.entity.BannerInfo;
import com.community.modules.content.entity.CommentInfo;
import com.community.modules.content.entity.ExchangeOrder;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.entity.ForumCategory;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.entity.InfoDynamic;
import com.community.modules.content.entity.MallProduct;
import com.community.modules.content.entity.NoticeInfo;
import com.community.modules.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private static final String TEST_DATA_MARKER_HEADER = "X-Test-Data-Marker";

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

    @Operation(summary = "Dynamic detail")
    @GetMapping("/dynamics/{id}")
    public ApiResponse<InfoDynamic> detailDynamic(@PathVariable Long id) {
        return ApiResponse.success(contentService.detailDynamic(id));
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

    @Operation(summary = "Admin - archive dynamic (delete alias)")
    @DeleteMapping("/dynamics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteDynamic(@PathVariable Long id) {
        contentService.deleteDynamic(id);
        return ApiResponse.success("archived", null);
    }

    @Operation(summary = "Admin - batch archive dynamics (delete alias)")
    @PostMapping("/dynamics/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteDynamics(@Valid @RequestBody IdListRequest request) {
        contentService.batchDeleteDynamics(request.getIds());
        return ApiResponse.success("batch archived", null);
    }

    @Operation(summary = "Admin - batch archive dynamics")
    @PostMapping("/dynamics/batch-archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchArchiveDynamics(@Valid @RequestBody IdListRequest request) {
        contentService.batchArchiveDynamics(request.getIds());
        return ApiResponse.success("batch archived", null);
    }

    @Operation(summary = "Admin - batch restore dynamics")
    @PostMapping("/dynamics/batch-restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchRestoreDynamics(@Valid @RequestBody IdListRequest request) {
        contentService.batchRestoreDynamics(request.getIds());
        return ApiResponse.success("batch restored", null);
    }

    @Operation(summary = "Page notices")
    @GetMapping("/notices/page")
    public ApiResponse<PageResult<NoticeInfo>> pageNotices(@RequestParam(defaultValue = "1") long current,
                                                           @RequestParam(defaultValue = "10") long size,
                                                           @RequestParam(defaultValue = "true") boolean onlyPublished) {
        return ApiResponse.success(contentService.pageNotices(current, size, onlyPublished));
    }

    @Operation(summary = "Notice detail")
    @GetMapping("/notices/{id}")
    public ApiResponse<NoticeInfo> detailNotice(@PathVariable Long id) {
        return ApiResponse.success(contentService.detailNotice(id));
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

    @Operation(summary = "Admin - archive notice (delete alias)")
    @DeleteMapping("/notices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteNotice(@PathVariable Long id) {
        contentService.deleteNotice(id);
        return ApiResponse.success("archived", null);
    }

    @Operation(summary = "Admin - batch archive notices (delete alias)")
    @PostMapping("/notices/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteNotices(@Valid @RequestBody IdListRequest request) {
        contentService.batchDeleteNotices(request.getIds());
        return ApiResponse.success("batch archived", null);
    }

    @Operation(summary = "Admin - batch archive notices")
    @PostMapping("/notices/batch-archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchArchiveNotices(@Valid @RequestBody IdListRequest request) {
        contentService.batchArchiveNotices(request.getIds());
        return ApiResponse.success("batch archived", null);
    }

    @Operation(summary = "Admin - batch restore notices")
    @PostMapping("/notices/batch-restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchRestoreNotices(@Valid @RequestBody IdListRequest request) {
        contentService.batchRestoreNotices(request.getIds());
        return ApiResponse.success("batch restored", null);
    }

    @Operation(summary = "List active banners")
    @GetMapping("/banners")
    public ApiResponse<List<BannerInfo>> banners() {
        return ApiResponse.success(contentService.listBanners());
    }

    @Operation(summary = "Admin - list banners")
    @GetMapping("/admin/banners")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BannerInfo>> adminBanners() {
        return ApiResponse.success(contentService.listBannersForAdmin());
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

    @Operation(summary = "Admin - delete banner")
    @DeleteMapping("/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        contentService.deleteBanner(id);
        return ApiResponse.success("deleted", null);
    }

    @Operation(summary = "Admin - batch delete banners")
    @PostMapping("/banners/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteBanners(@Valid @RequestBody IdListRequest request) {
        contentService.batchDeleteBanners(request.getIds());
        return ApiResponse.success("batch deleted", null);
    }

    @Operation(summary = "List forum categories")
    @GetMapping("/forum/categories")
    public ApiResponse<List<ForumCategory>> forumCategories() {
        return ApiResponse.success(contentService.listForumCategories());
    }

    @Operation(summary = "Admin - list forum categories")
    @GetMapping("/admin/forum/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ForumCategory>> adminForumCategories() {
        return ApiResponse.success(contentService.listForumCategoriesForAdmin());
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

    @Operation(summary = "Admin - delete forum category")
    @DeleteMapping("/forum/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteForumCategory(@PathVariable Long id) {
        contentService.deleteForumCategory(id);
        return ApiResponse.success("deleted", null);
    }

    @Operation(summary = "Admin - batch delete forum categories")
    @PostMapping("/forum/categories/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteForumCategories(@Valid @RequestBody IdListRequest request) {
        contentService.batchDeleteForumCategories(request.getIds());
        return ApiResponse.success("batch deleted", null);
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

    @Operation(summary = "Forum post detail")
    @GetMapping("/forum/posts/{id}")
    public ApiResponse<ForumPost> detailForumPost(@PathVariable Long id) {
        return ApiResponse.success(contentService.detailForumPost(id));
    }

    @Operation(summary = "Volunteer - create/update my post")
    @PostMapping("/forum/posts/my")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<ForumPost> saveOrUpdateMyPost(@RequestBody ForumPost post) {
        return ApiResponse.success("saved", contentService.saveOrUpdateMyPost(post));
    }

    @Operation(summary = "Volunteer - undo my submitted post")
    @DeleteMapping("/forum/posts/my/{id}/undo")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> undoMyPost(@PathVariable Long id) {
        contentService.undoMyPostSubmit(id);
        return ApiResponse.success("undone", null);
    }

    @Operation(summary = "Admin - update forum post")
    @PutMapping("/forum/posts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateForumPost(@PathVariable Long id, @RequestBody ForumPost post) {
        contentService.updateForumPost(id, post);
        return ApiResponse.success("updated", null);
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

    @Operation(summary = "Admin - archive post (delete alias)")
    @DeleteMapping("/forum/posts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        contentService.deleteForumPost(id);
        return ApiResponse.success("archived", null);
    }

    @Operation(summary = "Admin - batch archive posts (delete alias)")
    @PostMapping("/forum/posts/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeletePosts(@Valid @RequestBody IdListRequest request) {
        contentService.batchDeleteForumPosts(request.getIds());
        return ApiResponse.success("batch archived", null);
    }

    @Operation(summary = "Create comment")
    @PostMapping("/comments")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTEER')")
    public ApiResponse<Void> addComment(@RequestBody CommentInfo comment,
                                        @RequestHeader(value = TEST_DATA_MARKER_HEADER, required = false) String testDataMarker) {
        comment.setTestDataTag(normalizeTestDataMarker(testDataMarker));
        contentService.addComment(comment);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Page comments")
    @GetMapping("/comments/page")
    public ApiResponse<PageResult<CommentInfo>> pageComments(@RequestParam(defaultValue = "1") long current,
                                                             @RequestParam(defaultValue = "10") long size,
                                                             @RequestParam(defaultValue = "false") boolean onlyMine,
                                                             @RequestParam(required = false) String targetType,
                                                             @RequestParam(required = false) Long targetId,
                                                             @RequestParam(defaultValue = "false") boolean includeTestData,
                                                             Authentication authentication) {
        if (targetId != null && !StringUtils.hasText(targetType)) {
            throw new BusinessException(400, "按目标ID筛选评论时必须同时提供目标类型");
        }
        if (onlyMine && (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken)) {
            throw new BusinessException(401, "查询我的评论前请先登录");
        }
        return ApiResponse.success(contentService.pageComments(
                current,
                size,
                onlyMine,
                targetType,
                targetId,
                includeTestData && isAdmin(authentication)
        ));
    }

    @Operation(summary = "Admin - delete comment")
    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        contentService.deleteComment(id);
        return ApiResponse.success("deleted", null);
    }

    @Operation(summary = "Admin - batch delete comments")
    @PostMapping("/comments/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteComments(@Valid @RequestBody IdListRequest request) {
        contentService.batchDeleteComments(request.getIds());
        return ApiResponse.success("batch deleted", null);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String normalizeTestDataMarker(String testDataMarker) {
        String marker = StringUtils.trimWhitespace(testDataMarker);
        if (!StringUtils.hasText(marker)) {
            return null;
        }
        return marker.length() > 64 ? marker.substring(0, 64) : marker;
    }

    @Operation(summary = "My favorite activities")
    @GetMapping("/favorites")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<List<FavoriteActivity>> myFavorites() {
        return ApiResponse.success(contentService.myFavorites());
    }

    @Operation(summary = "Page my favorites")
    @GetMapping("/favorites/page")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<PageResult<FavoriteActivity>> pageFavorites(@RequestParam(defaultValue = "1") long current,
                                                                   @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(contentService.pageFavorites(current, size));
    }

    @Operation(summary = "Create favorite")
    @PostMapping("/favorites")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> createFavorite(@Valid @RequestBody FavoriteRequest request) {
        contentService.createFavorite(request);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Update favorite")
    @PutMapping("/favorites/{id}")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> updateFavorite(@PathVariable Long id, @RequestBody FavoriteUpdateRequest request) {
        contentService.updateFavorite(id, request);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Delete favorite by id")
    @DeleteMapping("/favorites/id/{id}")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> removeFavoriteById(@PathVariable Long id) {
        contentService.removeFavoriteById(id);
        return ApiResponse.success("removed", null);
    }

    @Operation(summary = "Batch delete favorites")
    @PostMapping("/favorites/batch-delete")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> batchRemoveFavorites(@Valid @RequestBody IdListRequest request) {
        contentService.batchRemoveFavoriteById(request.getIds());
        return ApiResponse.success("batch removed", null);
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

    @Operation(summary = "Page mall products")
    @GetMapping("/mall-products/page")
    public ApiResponse<PageResult<MallProduct>> pageMallProducts(@RequestParam(defaultValue = "1") long current,
                                                                 @RequestParam(defaultValue = "10") long size,
                                                                 @RequestParam(required = false) String keyword,
                                                                 @RequestParam(required = false) Integer status,
                                                                 @RequestParam(defaultValue = "true") boolean onlyEnabled) {
        return ApiResponse.success(contentService.pageMallProducts(current, size, keyword, status, onlyEnabled));
    }

    @Operation(summary = "Admin - page mall products")
    @GetMapping("/admin/mall-products/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<MallProduct>> pageAdminMallProducts(@RequestParam(defaultValue = "1") long current,
                                                                      @RequestParam(defaultValue = "10") long size,
                                                                      @RequestParam(required = false) String keyword,
                                                                      @RequestParam(required = false) Integer status) {
        return ApiResponse.success(contentService.pageMallProducts(current, size, keyword, status, false));
    }

    @Operation(summary = "Admin - create mall product")
    @PostMapping("/mall-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> saveMallProduct(@Valid @RequestBody MallProductRequest request) {
        contentService.saveMallProduct(request);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Admin - update mall product")
    @PutMapping("/mall-products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateMallProduct(@PathVariable Long id, @Valid @RequestBody MallProductRequest request) {
        contentService.updateMallProduct(id, request);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Admin - disable mall product")
    @DeleteMapping("/mall-products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> disableMallProduct(@PathVariable Long id) {
        contentService.disableMallProduct(id);
        return ApiResponse.success("disabled", null);
    }

    @Operation(summary = "Admin - enable mall product")
    @PutMapping("/mall-products/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> enableMallProduct(@PathVariable Long id) {
        contentService.enableMallProduct(id);
        return ApiResponse.success("enabled", null);
    }

    @Operation(summary = "Admin - batch disable mall products")
    @PostMapping("/mall-products/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDisableMallProducts(@Valid @RequestBody IdListRequest request) {
        contentService.batchDisableMallProducts(request.getIds());
        return ApiResponse.success("batch disabled", null);
    }

    @Operation(summary = "Admin - batch enable mall products")
    @PostMapping("/mall-products/batch-enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchEnableMallProducts(@Valid @RequestBody IdListRequest request) {
        contentService.batchEnableMallProducts(request.getIds());
        return ApiResponse.success("batch enabled", null);
    }

    @Operation(summary = "Volunteer - page exchange orders")
    @GetMapping("/orders/page")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<PageResult<ExchangeOrder>> pageMyExchangeOrders(@RequestParam(defaultValue = "1") long current,
                                                                       @RequestParam(defaultValue = "10") long size,
                                                                       @RequestParam(required = false) String status) {
        return ApiResponse.success(contentService.pageMyExchangeOrders(current, size, status));
    }

    @Operation(summary = "Volunteer - create exchange order")
    @PostMapping("/orders")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<ExchangeOrder> createExchangeOrder(@Valid @RequestBody ExchangeOrderCreateRequest request) {
        return ApiResponse.success("created", contentService.createExchangeOrder(request));
    }

    @Operation(summary = "Admin - page exchange orders")
    @GetMapping("/admin/orders/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<ExchangeOrder>> pageAdminExchangeOrders(@RequestParam(defaultValue = "1") long current,
                                                                          @RequestParam(defaultValue = "10") long size,
                                                                          @RequestParam(required = false) String orderNo,
                                                                          @RequestParam(required = false) String productKeyword,
                                                                          @RequestParam(required = false) String userKeyword,
                                                                          @RequestParam(required = false) String status) {
        return ApiResponse.success(contentService.pageAdminExchangeOrders(
                current,
                size,
                orderNo,
                productKeyword,
                userKeyword,
                status
        ));
    }

    @Operation(summary = "Admin - exchange order detail")
    @GetMapping("/admin/orders/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExchangeOrder> detailAdminExchangeOrder(@PathVariable Long id) {
        return ApiResponse.success(contentService.detailAdminExchangeOrder(id));
    }

    @Operation(summary = "Admin - update exchange order status")
    @PutMapping("/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateExchangeOrderStatus(@PathVariable Long id,
                                                       @Valid @RequestBody ExchangeOrderStatusRequest request) {
        contentService.updateExchangeOrderStatus(id, request);
        return ApiResponse.success("updated", null);
    }
}
