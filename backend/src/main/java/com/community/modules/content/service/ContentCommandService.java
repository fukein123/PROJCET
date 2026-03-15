package com.community.modules.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.exception.ApiErrorCode;
import com.community.common.util.SecurityUtil;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.mapper.ActivityMapper;
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
import com.community.modules.content.mapper.BannerInfoMapper;
import com.community.modules.content.mapper.CommentInfoMapper;
import com.community.modules.content.mapper.ExchangeOrderMapper;
import com.community.modules.content.mapper.FavoriteActivityMapper;
import com.community.modules.content.mapper.ForumCategoryMapper;
import com.community.modules.content.mapper.ForumPostMapper;
import com.community.modules.content.mapper.InfoDynamicMapper;
import com.community.modules.content.mapper.MallProductMapper;
import com.community.modules.content.mapper.NoticeInfoMapper;
import com.community.modules.user.entity.PointsChangeLog;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.PointsChangeLogMapper;
import com.community.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ContentCommandService {

    private static final Duration POST_UNDO_WINDOW = Duration.ofSeconds(30);
    private static final String ORDER_STATUS_CREATED = "CREATED";
    private static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    private static final String ORDER_STATUS_RECEIVED = "RECEIVED";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final DateTimeFormatter SERIAL_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final InfoDynamicMapper infoDynamicMapper;
    private final NoticeInfoMapper noticeInfoMapper;
    private final BannerInfoMapper bannerInfoMapper;
    private final ForumCategoryMapper forumCategoryMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumModerationService forumModerationService;
    private final CommentInfoMapper commentInfoMapper;
    private final FavoriteActivityMapper favoriteActivityMapper;
    private final ActivityMapper activityMapper;
    private final MallProductMapper mallProductMapper;
    private final ExchangeOrderMapper exchangeOrderMapper;
    private final UserMapper userMapper;
    private final PointsChangeLogMapper pointsChangeLogMapper;
    private final AdminOperationLogService adminOperationLogService;

    private final AtomicInteger orderSequence = new AtomicInteger();
    private final AtomicInteger pointsSequence = new AtomicInteger();
    private LocalDate orderSequenceDate = LocalDate.now();
    private LocalDate pointsSequenceDate = LocalDate.now();

    public void saveDynamic(InfoDynamic dynamic) {
        dynamic.setTitle(requireText(dynamic.getTitle(), "Dynamic title is required"));
        dynamic.setSource(requireText(dynamic.getSource(), "Dynamic source is required"));
        dynamic.setContent(requireRichContent(dynamic.getContent(), "Dynamic content is required"));
        dynamic.setImageUrl(normalizeOptionalText(dynamic.getImageUrl()));
        dynamic.setType(normalizeType(dynamic.getType(), "NEWS"));
        dynamic.setViews(dynamic.getViews() == null ? 0 : dynamic.getViews());
        dynamic.setStatus(dynamic.getStatus() == null ? 1 : dynamic.getStatus());
        dynamic.setPublishTime(dynamic.getPublishTime() == null ? LocalDateTime.now() : dynamic.getPublishTime());
        dynamic.setAuthorId(dynamic.getAuthorId() == null ? SecurityUtil.currentUserId() : dynamic.getAuthorId());
        infoDynamicMapper.insert(dynamic);
    }

    public void updateDynamic(Long id, InfoDynamic dynamic) {
        InfoDynamic db = infoDynamicMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "Dynamic not found");
        }
        db.setTitle(requireText(dynamic.getTitle(), "Dynamic title is required"));
        db.setSource(requireText(dynamic.getSource(), "Dynamic source is required"));
        db.setContent(requireRichContent(dynamic.getContent(), "Dynamic content is required"));
        db.setImageUrl(normalizeOptionalText(dynamic.getImageUrl()));
        db.setType(normalizeType(dynamic.getType(), db.getType()));
        db.setStatus(dynamic.getStatus() == null ? db.getStatus() : dynamic.getStatus());
        db.setPublishTime(dynamic.getPublishTime() == null ? db.getPublishTime() : dynamic.getPublishTime());
        infoDynamicMapper.updateById(db);
    }

    public void deleteDynamic(Long id) {
        InfoDynamic dynamic = infoDynamicMapper.selectById(id);
        archiveDynamic(dynamic, "delete alias -> archived");
    }

    public void batchDeleteDynamics(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::deleteDynamic);
    }

    public void batchArchiveDynamics(List<Long> ids) {
        batchUpdateDynamicStatus(ids, 0);
        adminOperationLogService.record("ARCHIVE_DYNAMIC_BATCH", "DYNAMIC", null, "batch", "ids=" + summarizeIds(ids));
    }

    public void batchRestoreDynamics(List<Long> ids) {
        batchUpdateDynamicStatus(ids, 1);
    }

    public void increaseDynamicViews(Long id) {
        infoDynamicMapper.update(null, new UpdateWrapper<InfoDynamic>()
                .eq("id", id)
                .setSql("views = coalesce(views, 0) + 1"));
    }

    public void saveNotice(NoticeInfo notice) {
        notice.setTitle(requireText(notice.getTitle(), "Notice title is required"));
        notice.setContent(requireRichContent(notice.getContent(), "Notice content is required"));
        notice.setStatus(notice.getStatus() == null ? 1 : notice.getStatus());
        notice.setPublishTime(notice.getPublishTime() == null ? LocalDateTime.now() : notice.getPublishTime());
        noticeInfoMapper.insert(notice);
    }

    public void updateNotice(Long id, NoticeInfo notice) {
        NoticeInfo db = noticeInfoMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "Notice not found");
        }
        db.setTitle(requireText(notice.getTitle(), "Notice title is required"));
        db.setContent(requireRichContent(notice.getContent(), "Notice content is required"));
        db.setStatus(notice.getStatus() == null ? db.getStatus() : notice.getStatus());
        db.setPublishTime(notice.getPublishTime() == null ? db.getPublishTime() : notice.getPublishTime());
        noticeInfoMapper.updateById(db);
    }

    public void deleteNotice(Long id) {
        NoticeInfo notice = noticeInfoMapper.selectById(id);
        archiveNotice(notice, "delete alias -> archived");
    }

    public void batchDeleteNotices(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::deleteNotice);
    }

    public void batchArchiveNotices(List<Long> ids) {
        batchUpdateNoticeStatus(ids, 0);
        adminOperationLogService.record("ARCHIVE_NOTICE_BATCH", "NOTICE", null, "batch", "ids=" + summarizeIds(ids));
    }

    public void batchRestoreNotices(List<Long> ids) {
        batchUpdateNoticeStatus(ids, 1);
    }

    public void saveBanner(BannerInfo banner) {
        banner.setTitle(requireText(banner.getTitle(), "Banner title is required"));
        banner.setImageUrl(requireText(banner.getImageUrl(), "Banner image is required"));
        banner.setActivityId(normalizeOptionalActivityId(banner.getActivityId()));
        banner.setStatus(normalizeBinaryStatus(banner.getStatus(), 1, "Banner status is invalid"));
        banner.setSort(normalizeNonNegativeNumber(banner.getSort(), 0, "Banner sort must be greater than or equal to 0"));
        bannerInfoMapper.insert(banner);
    }

    public void updateBanner(Long id, BannerInfo banner) {
        BannerInfo db = bannerInfoMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "Banner not found");
        }
        db.setTitle(requireText(banner.getTitle(), "Banner title is required"));
        db.setImageUrl(requireText(banner.getImageUrl(), "Banner image is required"));
        db.setActivityId(normalizeOptionalActivityId(banner.getActivityId()));
        db.setSort(normalizeNonNegativeNumber(banner.getSort(), db.getSort(), "Banner sort must be greater than or equal to 0"));
        db.setStatus(normalizeBinaryStatus(banner.getStatus(), db.getStatus(), "Banner status is invalid"));
        bannerInfoMapper.updateById(db);
    }

    public void deleteBanner(Long id) {
        bannerInfoMapper.deleteById(id);
    }

    public void batchDeleteBanners(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> validIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (validIds.isEmpty()) {
            return;
        }
        bannerInfoMapper.deleteByIds(validIds);
    }

    public void saveForumCategory(ForumCategory category) {
        category.setName(requireText(category.getName(), "Forum category name is required"));
        category.setStatus(normalizeBinaryStatus(category.getStatus(), 1, "Forum category status is invalid"));
        category.setSort(normalizeNonNegativeNumber(
                category.getSort(),
                0,
                "Forum category sort must be greater than or equal to 0"
        ));
        forumCategoryMapper.insert(category);
    }

    public void updateForumCategory(Long id, ForumCategory category) {
        ForumCategory db = forumCategoryMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "Forum category not found");
        }
        db.setName(requireText(category.getName(), "Forum category name is required"));
        db.setSort(normalizeNonNegativeNumber(
                category.getSort(),
                db.getSort(),
                "Forum category sort must be greater than or equal to 0"
        ));
        db.setStatus(normalizeBinaryStatus(category.getStatus(), db.getStatus(), "Forum category status is invalid"));
        forumCategoryMapper.updateById(db);
    }

    public void deleteForumCategory(Long id) {
        List<ForumPost> posts = forumPostMapper.selectList(new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getCategoryId, id));
        if (!posts.isEmpty()) {
            throw new BusinessException(
                    ApiErrorCode.BUSINESS_CONFLICT,
                    "Forum category still has associated posts and cannot be deleted"
            );
        }
        forumCategoryMapper.deleteById(id);
    }

    public void batchDeleteForumCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::deleteForumCategory);
    }

    public ForumPost saveOrUpdateMyPost(ForumPost post) {
        Long userId = SecurityUtil.currentUserId();
        post.setTitle(requireText(post.getTitle(), "Post title is required"));
        post.setCoverImage(requireText(post.getCoverImage(), "Post cover image is required"));
        post.setSummary(requireText(post.getSummary(), "Post summary is required"));
        post.setContent(requireRichContent(post.getContent(), "Post content is required"));
        if (post.getCategoryId() == null) {
            throw new BusinessException(400, "Post category is required");
        }

        if (post.getId() == null) {
            post.setUserId(userId);
            post.setStatus("PENDING");
            post.setViews(post.getViews() == null ? 0 : post.getViews());
            forumPostMapper.insert(post);
            return forumPostMapper.selectById(post.getId());
        }

        ForumPost db = forumPostMapper.selectById(post.getId());
        if (db == null) {
            throw new BusinessException(404, "Post not found");
        }
        if (!db.getUserId().equals(userId)) {
            throw new BusinessException(403, "Cannot edit another user's post");
        }

        db.setTitle(post.getTitle());
        db.setCoverImage(post.getCoverImage());
        db.setSummary(post.getSummary());
        db.setContent(post.getContent());
        db.setCategoryId(post.getCategoryId());
        db.setStatus("PENDING");
        db.setAuditReason(null);
        forumPostMapper.updateById(db);
        return forumPostMapper.selectById(db.getId());
    }

    public void undoMyPostSubmit(Long postId) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "Post not found");
        }

        Long currentUserId = SecurityUtil.currentUserId();
        if (!Objects.equals(post.getUserId(), currentUserId)) {
            throw new BusinessException(403, "Cannot undo another user's post");
        }
        if (!"PENDING".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("Only pending posts can be undone");
        }
        if (post.getCreateTime() == null || post.getCreateTime().plus(POST_UNDO_WINDOW).isBefore(LocalDateTime.now())) {
            throw new BusinessException("Post undo window has expired");
        }

        forumPostMapper.deleteById(postId);
    }

    public void updateForumPostByAdmin(Long id, ForumPost post) {
        ForumPost db = forumPostMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "Post not found");
        }
        if (post.getCategoryId() == null || forumCategoryMapper.selectById(post.getCategoryId()) == null) {
            throw new BusinessException(400, "Post category is required");
        }

        db.setTitle(requireText(post.getTitle(), "Post title is required"));
        db.setCoverImage(requireText(post.getCoverImage(), "Post cover image is required"));
        db.setSummary(requireText(post.getSummary(), "Post summary is required"));
        db.setContent(requireRichContent(post.getContent(), "Post content is required"));
        db.setCategoryId(post.getCategoryId());

        String nextStatus = normalizeForumPostStatus(post.getStatus(), db.getStatus());
        db.setStatus(nextStatus);
        if ("REJECTED".equals(nextStatus)) {
            db.setAuditReason(requireText(post.getAuditReason(), "Reject reason is required when status is REJECTED"));
        } else if ("PENDING".equals(nextStatus)) {
            db.setAuditReason(null);
        } else {
            db.setAuditReason(normalizeOptionalText(post.getAuditReason()));
        }

        forumPostMapper.updateById(db);
        adminOperationLogService.record("UPDATE_POST", "POST", db.getId(), db.getTitle(), "status=" + db.getStatus());
    }

    public void auditPost(Long id, String status, String reason) {
        forumModerationService.auditPost(id, status, reason);
    }

    public void deleteForumPost(Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        archiveForumPost(post, "delete alias -> archived");
    }

    public void batchDeleteForumPosts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).forEach(this::deleteForumPost);
    }

    public void increaseForumPostViews(Long id) {
        forumPostMapper.update(null, new UpdateWrapper<ForumPost>()
                .eq("id", id)
                .setSql("views = coalesce(views, 0) + 1"));
    }

    public void addComment(CommentInfo comment) {
        String normalizedContent = requireRichContent(comment.getContent(), "Comment content is required");
        assertCommentTarget(comment.getTargetType(), comment.getTargetId());
        comment.setUserId(SecurityUtil.currentUserId());
        comment.setContent(normalizedContent);
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
            throw new BusinessException(404, "Favorite record not found");
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

    public void saveMallProduct(MallProductRequest request) {
        MallProduct product = new MallProduct();
        product.setName(requireText(request.getName(), "Product name is required"));
        product.setImageUrl(requireText(request.getImageUrl(), "Product image is required"));
        product.setSummary(requireText(request.getSummary(), "Product summary is required"));
        product.setPointsCost(normalizeNonNegativeNumber(
                request.getPointsCost(),
                0,
                "Points cost must be greater than or equal to 0"
        ));
        product.setStock(normalizeNonNegativeNumber(
                request.getStock(),
                0,
                "Product stock must be greater than or equal to 0"
        ));
        product.setStatus(normalizeBinaryStatus(request.getStatus(), 1, "Product status is invalid"));
        mallProductMapper.insert(product);
    }

    public void updateMallProduct(Long id, MallProductRequest request) {
        MallProduct product = requireMallProduct(id);
        product.setName(requireText(request.getName(), "Product name is required"));
        product.setImageUrl(requireText(request.getImageUrl(), "Product image is required"));
        product.setSummary(requireText(request.getSummary(), "Product summary is required"));
        product.setPointsCost(normalizeNonNegativeNumber(
                request.getPointsCost(),
                product.getPointsCost(),
                "Points cost must be greater than or equal to 0"
        ));
        product.setStock(normalizeNonNegativeNumber(
                request.getStock(),
                product.getStock(),
                "Product stock must be greater than or equal to 0"
        ));
        product.setStatus(normalizeBinaryStatus(request.getStatus(), product.getStatus(), "Product status is invalid"));
        mallProductMapper.updateById(product);
    }

    public void disableMallProduct(Long id) {
        MallProduct product = requireMallProduct(id);
        product.setStatus(0);
        mallProductMapper.updateById(product);
        adminOperationLogService.record("DISABLE_MALL_PRODUCT", "MALL_PRODUCT", product.getId(), product.getName(), "status=0");
    }

    public void enableMallProduct(Long id) {
        MallProduct product = requireMallProduct(id);
        product.setStatus(1);
        mallProductMapper.updateById(product);
    }

    public void batchDisableMallProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::disableMallProduct);
    }

    public void batchEnableMallProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::enableMallProduct);
    }

    public ExchangeOrder createExchangeOrder(ExchangeOrderCreateRequest request) {
        Long userId = SecurityUtil.currentUserId();
        User user = requireEnabledUser(userId);
        MallProduct product = requireEnabledMallProduct(request.getProductId());
        String requestKey = requireText(request.getRequestKey(), "Request key is required");
        ExchangeOrder existing = exchangeOrderMapper.selectOne(new LambdaQueryWrapper<ExchangeOrder>()
                .eq(ExchangeOrder::getUserId, userId)
                .eq(ExchangeOrder::getRequestKey, requestKey)
                .last("limit 1"));
        if (existing != null) {
            return existing;
        }

        int quantity = normalizePositiveNumber(request.getQuantity(), "Quantity must be greater than 0");
        int totalPoints;
        try {
            totalPoints = Math.multiplyExact(defaultZero(product.getPointsCost()), quantity);
        } catch (ArithmeticException ex) {
            throw new BusinessException(ApiErrorCode.REQUEST_INVALID, "Total points exceeds the supported range");
        }
        if (defaultZero(user.getPoints()) < totalPoints) {
            throw new BusinessException(ApiErrorCode.BUSINESS_CONFLICT, "Current points are not enough for this exchange");
        }

        ExchangeOrder order = new ExchangeOrder();
        order.setOrderNo(nextOrderNo());
        order.setUserId(user.getId());
        order.setProductId(product.getId());
        order.setUserName(user.getUsername());
        order.setRealName(normalizeOptionalText(user.getRealName()));
        order.setProductName(product.getName());
        order.setProductImage(product.getImageUrl());
        order.setProductSummary(product.getSummary());
        order.setQuantity(quantity);
        order.setPointsPerItem(defaultZero(product.getPointsCost()));
        order.setTotalPoints(totalPoints);
        order.setReceiverName(requireText(request.getReceiverName(), "Receiver name is required"));
        order.setReceiverPhone(requireText(request.getReceiverPhone(), "Receiver phone is required"));
        order.setReceiverAddress(requireText(request.getReceiverAddress(), "Receiver address is required"));
        order.setRequestKey(requestKey);
        order.setStatus(ORDER_STATUS_CREATED);

        try {
            exchangeOrderMapper.insert(order);
        } catch (DuplicateKeyException ex) {
            ExchangeOrder duplicate = exchangeOrderMapper.selectOne(new LambdaQueryWrapper<ExchangeOrder>()
                    .eq(ExchangeOrder::getUserId, userId)
                    .eq(ExchangeOrder::getRequestKey, requestKey)
                    .last("limit 1"));
            if (duplicate != null) {
                return duplicate;
            }
            throw ex;
        }

        int userUpdated = userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", userId)
                .ge("points", totalPoints)
                .setSql("points = coalesce(points, 0) - " + totalPoints));
        if (userUpdated != 1) {
            throw new BusinessException(ApiErrorCode.BUSINESS_CONFLICT, "Current points are not enough for this exchange");
        }

        int stockUpdated = mallProductMapper.update(null, new UpdateWrapper<MallProduct>()
                .eq("id", product.getId())
                .eq("status", 1)
                .ge("stock", quantity)
                .setSql("stock = stock - " + quantity));
        if (stockUpdated != 1) {
            throw new BusinessException(ApiErrorCode.BUSINESS_CONFLICT, "Current product stock is not enough");
        }

        pointsChangeLogMapper.insert(buildPointsLog(
                userId,
                "DEBIT",
                totalPoints,
                "ORDER_EXCHANGE",
                order.getId(),
                order.getOrderNo(),
                "Mall exchange deduction"
        ));
        return order;
    }

    public void updateExchangeOrderStatus(Long id, ExchangeOrderStatusRequest request) {
        ExchangeOrder order = requireExchangeOrder(id);
        String nextStatus = requireText(request.getStatus(), "Order status is required").toUpperCase();
        String reason = normalizeOptionalText(request.getReason());
        String currentStatus = normalizeOptionalText(order.getStatus());

        if (ORDER_STATUS_CREATED.equalsIgnoreCase(currentStatus)) {
            if (ORDER_STATUS_SHIPPED.equals(nextStatus)) {
                order.setStatus(ORDER_STATUS_SHIPPED);
                order.setStatusReason(null);
                order.setShippedTime(LocalDateTime.now());
                exchangeOrderMapper.updateById(order);
                adminOperationLogService.record(
                        "UPDATE_ORDER_STATUS",
                        "ORDER",
                        order.getId(),
                        order.getOrderNo(),
                        "status=SHIPPED"
                );
                return;
            }
            if (ORDER_STATUS_CANCELLED.equals(nextStatus)) {
                restoreOrderResources(order);
                order.setStatus(ORDER_STATUS_CANCELLED);
                order.setStatusReason(reason);
                order.setCancelledTime(LocalDateTime.now());
                exchangeOrderMapper.updateById(order);
                adminOperationLogService.record(
                        "UPDATE_ORDER_STATUS",
                        "ORDER",
                        order.getId(),
                        order.getOrderNo(),
                        "status=CANCELLED" + (StringUtils.hasText(reason) ? ", reason=" + reason : "")
                );
                return;
            }
        }

        if (ORDER_STATUS_SHIPPED.equalsIgnoreCase(currentStatus) && ORDER_STATUS_RECEIVED.equals(nextStatus)) {
            order.setStatus(ORDER_STATUS_RECEIVED);
            order.setStatusReason(null);
            order.setReceivedTime(LocalDateTime.now());
            exchangeOrderMapper.updateById(order);
            adminOperationLogService.record(
                    "UPDATE_ORDER_STATUS",
                    "ORDER",
                    order.getId(),
                    order.getOrderNo(),
                    "status=RECEIVED"
            );
            return;
        }

        throw new BusinessException(ApiErrorCode.BUSINESS_CONFLICT, "Order status transition is not allowed");
    }

    private Activity requireActivity(Long activityId) {
        if (activityId == null) {
            throw new BusinessException(400, "Please select an activity first");
        }
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "Activity not found");
        }
        return activity;
    }

    private MallProduct requireMallProduct(Long id) {
        MallProduct product = mallProductMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "Mall product not found");
        }
        return product;
    }

    private MallProduct requireEnabledMallProduct(Long id) {
        MallProduct product = requireMallProduct(id);
        if (!Objects.equals(product.getStatus(), 1)) {
            throw new BusinessException(ApiErrorCode.BUSINESS_CONFLICT, "Current product is unavailable");
        }
        return product;
    }

    private User requireEnabledUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ApiErrorCode.UNAUTHORIZED, "Please log in first");
        }
        User user = userMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getStatus(), 1)) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "Current user not found");
        }
        return user;
    }

    private ExchangeOrder requireExchangeOrder(Long id) {
        ExchangeOrder order = exchangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "Exchange order not found");
        }
        return order;
    }

    private void assertCommentTarget(String targetType, Long targetId) {
        if (!StringUtils.hasText(targetType) || targetId == null) {
            throw new BusinessException(400, "Comment target is incomplete");
        }
        if ("ACTIVITY".equalsIgnoreCase(targetType)) {
            requireActivity(targetId);
            return;
        }
        if ("POST".equalsIgnoreCase(targetType)) {
            ForumPost post = forumPostMapper.selectById(targetId);
            if (post == null) {
                throw new BusinessException(404, "Post not found");
            }
            return;
        }
        throw new BusinessException("Unsupported comment target type");
    }

    private int defaultPriority(Integer priority) {
        if (priority == null) {
            return 0;
        }
        return Math.max(0, Math.min(priority, 5));
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int normalizePositiveNumber(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new BusinessException(ApiErrorCode.REQUEST_INVALID, message);
        }
        return value;
    }

    private int normalizeNonNegativeNumber(Integer value, Integer fallback, String message) {
        if (value == null) {
            return fallback == null ? 0 : fallback;
        }
        if (value < 0) {
            throw new BusinessException(ApiErrorCode.REQUEST_INVALID, message);
        }
        return value;
    }

    private int normalizeBinaryStatus(Integer value, Integer fallback, String message) {
        if (value == null) {
            return fallback == null ? 1 : fallback;
        }
        if (value != 0 && value != 1) {
            throw new BusinessException(ApiErrorCode.REQUEST_INVALID, message);
        }
        return value;
    }

    private Long normalizeOptionalActivityId(Long activityId) {
        if (activityId == null || activityId <= 0) {
            return null;
        }
        requireActivity(activityId);
        return activityId;
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, message);
        }
        return value.trim();
    }

    private String requireRichContent(String value, String message) {
        String normalized = requireText(value, message);
        String plainText = normalized
                .replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                .replaceAll("(?i)<br\\s*/?>", " ")
                .replaceAll("(?i)</p>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
        boolean hasImage = normalized.toLowerCase(Locale.ROOT).contains("<img");
        if (!StringUtils.hasText(plainText) && !hasImage) {
            throw new BusinessException(400, message);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeType(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        return value.trim();
    }

    private String normalizeForumPostStatus(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        String normalized = value.trim().toUpperCase();
        if (!"PENDING".equals(normalized)
                && !"APPROVED".equals(normalized)
                && !"REJECTED".equals(normalized)
                && !"ARCHIVED".equals(normalized)) {
            throw new BusinessException(400, "Post status is invalid");
        }
        return normalized;
    }

    private void archiveDynamic(InfoDynamic dynamic, String detail) {
        if (dynamic == null || Objects.equals(dynamic.getStatus(), 0)) {
            return;
        }
        dynamic.setStatus(0);
        infoDynamicMapper.updateById(dynamic);
        adminOperationLogService.record("ARCHIVE_DYNAMIC", "DYNAMIC", dynamic.getId(), dynamic.getTitle(), detail);
    }

    private void archiveNotice(NoticeInfo notice, String detail) {
        if (notice == null || Objects.equals(notice.getStatus(), 0)) {
            return;
        }
        notice.setStatus(0);
        noticeInfoMapper.updateById(notice);
        adminOperationLogService.record("ARCHIVE_NOTICE", "NOTICE", notice.getId(), notice.getTitle(), detail);
    }

    private void archiveForumPost(ForumPost post, String detail) {
        if (post == null || "ARCHIVED".equalsIgnoreCase(post.getStatus())) {
            return;
        }
        post.setStatus("ARCHIVED");
        forumPostMapper.updateById(post);
        adminOperationLogService.record("ARCHIVE_POST", "POST", post.getId(), post.getTitle(), detail);
    }

    private String summarizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        return ids.stream().filter(Objects::nonNull).distinct().limit(20).toList().toString();
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

    private synchronized String nextOrderNo() {
        LocalDate today = LocalDate.now();
        if (!today.equals(orderSequenceDate)) {
            orderSequenceDate = today;
            orderSequence.set(0);
        }
        int sequence = orderSequence.updateAndGet(current -> current >= 999 ? 1 : current + 1);
        return "ORD" + LocalDateTime.now().format(SERIAL_TIME_FORMAT) + String.format("%03d", sequence);
    }

    private synchronized String nextPointsChangeNo() {
        LocalDate today = LocalDate.now();
        if (!today.equals(pointsSequenceDate)) {
            pointsSequenceDate = today;
            pointsSequence.set(0);
        }
        int sequence = pointsSequence.updateAndGet(current -> current >= 999 ? 1 : current + 1);
        return "PTS" + LocalDateTime.now().format(SERIAL_TIME_FORMAT) + String.format("%03d", sequence);
    }

    private PointsChangeLog buildPointsLog(Long userId,
                                           String direction,
                                           Integer deltaPoints,
                                           String sourceType,
                                           Long sourceId,
                                           String referenceNo,
                                           String note) {
        PointsChangeLog log = new PointsChangeLog();
        log.setChangeNo(nextPointsChangeNo());
        log.setUserId(userId);
        log.setChangeDirection(direction);
        log.setDeltaPoints(defaultZero(deltaPoints));
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setReferenceNo(referenceNo);
        log.setNote(note);
        return log;
    }

    private void restoreOrderResources(ExchangeOrder order) {
        int quantity = normalizePositiveNumber(order.getQuantity(), "Order quantity is invalid");
        int totalPoints = defaultZero(order.getTotalPoints());
        if (order.getProductId() != null) {
            mallProductMapper.update(null, new UpdateWrapper<MallProduct>()
                    .eq("id", order.getProductId())
                    .setSql("stock = coalesce(stock, 0) + " + quantity));
        }
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", order.getUserId())
                .setSql("points = coalesce(points, 0) + " + totalPoints));
        pointsChangeLogMapper.insert(buildPointsLog(
                order.getUserId(),
                "CREDIT",
                totalPoints,
                "ORDER_CANCEL",
                order.getId(),
                order.getOrderNo(),
                "Mall order cancelled and points restored"
        ));
    }
}
