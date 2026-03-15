package com.community.modules.content;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.common.constant.SecurityConstants;
import com.community.common.exception.ApiErrorCode;
import com.community.common.exception.BusinessException;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.content.dto.FavoriteRequest;
import com.community.modules.content.dto.ExchangeOrderCreateRequest;
import com.community.modules.content.dto.ExchangeOrderStatusRequest;
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
import com.community.modules.content.service.ContentCommandService;
import com.community.modules.content.service.ForumModerationService;
import com.community.modules.user.entity.PointsChangeLog;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.PointsChangeLogMapper;
import com.community.modules.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentCommandServiceTest {

    @Mock
    private InfoDynamicMapper infoDynamicMapper;
    @Mock
    private NoticeInfoMapper noticeInfoMapper;
    @Mock
    private BannerInfoMapper bannerInfoMapper;
    @Mock
    private ForumCategoryMapper forumCategoryMapper;
    @Mock
    private ForumPostMapper forumPostMapper;
    @Mock
    private ForumModerationService forumModerationService;
    @Mock
    private CommentInfoMapper commentInfoMapper;
    @Mock
    private FavoriteActivityMapper favoriteActivityMapper;
    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private MallProductMapper mallProductMapper;
    @Mock
    private ExchangeOrderMapper exchangeOrderMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PointsChangeLogMapper pointsChangeLogMapper;
    @Mock
    private AdminOperationLogService adminOperationLogService;

    @InjectMocks
    private ContentCommandService contentCommandService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createFavorite_shouldUpdateExistingFavoriteInsteadOfInsert() {
        mockCurrentUser(9L);
        FavoriteRequest request = new FavoriteRequest();
        request.setActivityId(21L);
        request.setNote("Weekend join");
        request.setTag("High priority");
        request.setPriority(7);

        Activity activity = new Activity();
        activity.setId(21L);
        activity.setTitle("Weekend River Patrol");
        activity.setAddress("Riverside Service Center");
        activity.setStartTime(LocalDateTime.of(2026, 3, 16, 9, 0));
        activity.setEndTime(LocalDateTime.of(2026, 3, 16, 11, 0));
        when(activityMapper.selectById(21L)).thenReturn(activity);

        FavoriteActivity existing = new FavoriteActivity();
        existing.setId(88L);
        existing.setUserId(9L);
        existing.setActivityId(21L);
        existing.setPriority(1);
        when(favoriteActivityMapper.selectOne(any())).thenReturn(existing);

        contentCommandService.createFavorite(request);

        ArgumentCaptor<FavoriteActivity> favoriteCaptor = ArgumentCaptor.forClass(FavoriteActivity.class);
        verify(favoriteActivityMapper).updateById(favoriteCaptor.capture());
        verify(favoriteActivityMapper, never()).insert(any(FavoriteActivity.class));
        FavoriteActivity updated = favoriteCaptor.getValue();
        assertEquals(88L, updated.getId());
        assertEquals("Weekend join", updated.getNote());
        assertEquals("High priority", updated.getTag());
        assertEquals(5, updated.getPriority());
        assertEquals("Weekend River Patrol", updated.getActivityTitle());
        assertEquals("Riverside Service Center", updated.getActivityAddress());
        assertEquals(LocalDateTime.of(2026, 3, 16, 9, 0), updated.getActivityStartTime());
        assertEquals(LocalDateTime.of(2026, 3, 16, 11, 0), updated.getActivityEndTime());
    }

    @Test
    void saveOrUpdateMyPost_shouldRejectEditingOthersPost() {
        mockCurrentUser(9L);
        ForumPost request = new ForumPost();
        request.setId(31L);
        request.setTitle("Updated post");
        request.setCoverImage("https://example.com/post-cover.jpg");
        request.setSummary("Updated summary");
        request.setContent("Content");
        request.setCategoryId(7L);

        ForumPost existing = new ForumPost();
        existing.setId(31L);
        existing.setUserId(12L);
        when(forumPostMapper.selectById(31L)).thenReturn(existing);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> contentCommandService.saveOrUpdateMyPost(request)
        );

        assertEquals(ApiErrorCode.FORBIDDEN.getCode(), ex.getCode());
        assertEquals("Cannot edit another user's post", ex.getMessage());
        verify(forumPostMapper, never()).updateById(any(ForumPost.class));
    }

    @Test
    void addComment_shouldRejectUnsupportedTargetType() {
        CommentInfo comment = new CommentInfo();
        comment.setTargetType("NOTICE");
        comment.setTargetId(45L);
        comment.setContent("Just watching");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> contentCommandService.addComment(comment)
        );

        assertEquals("Unsupported comment target type", ex.getMessage());
        verify(commentInfoMapper, never()).insert(any(CommentInfo.class));
    }

    @Test
    void addComment_shouldPersistTrimmedContentAndResolvedTestMarker() {
        mockCurrentUser(9L);
        Activity activity = new Activity();
        activity.setId(22L);
        when(activityMapper.selectById(22L)).thenReturn(activity);

        CommentInfo comment = new CommentInfo();
        comment.setTargetType("ACTIVITY");
        comment.setTargetId(22L);
        comment.setContent("  Great activity  ");
        comment.setTestDataTag("smoke:add-comment");

        contentCommandService.addComment(comment);

        ArgumentCaptor<CommentInfo> commentCaptor = ArgumentCaptor.forClass(CommentInfo.class);
        verify(commentInfoMapper).insert(commentCaptor.capture());
        CommentInfo saved = commentCaptor.getValue();
        assertEquals(9L, saved.getUserId());
        assertEquals("Great activity", saved.getContent());
        assertEquals(1, saved.getStatus());
        assertEquals("smoke:add-comment", saved.getTestDataTag());
    }

    @Test
    void saveDynamic_shouldRejectBlankTitle() {
        InfoDynamic dynamic = new InfoDynamic();
        dynamic.setTitle("   ");
        dynamic.setSource("Community Center");
        dynamic.setContent("valid");

        BusinessException ex = assertThrows(BusinessException.class, () -> contentCommandService.saveDynamic(dynamic));

        assertEquals(ApiErrorCode.REQUEST_INVALID.getCode(), ex.getCode());
        assertEquals("Dynamic title is required", ex.getMessage());
        verify(infoDynamicMapper, never()).insert(any(InfoDynamic.class));
    }

    @Test
    void saveNotice_shouldRejectBlankContent() {
        NoticeInfo notice = new NoticeInfo();
        notice.setTitle("notice");
        notice.setContent("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> contentCommandService.saveNotice(notice));

        assertEquals(ApiErrorCode.REQUEST_INVALID.getCode(), ex.getCode());
        assertEquals("Notice content is required", ex.getMessage());
        verify(noticeInfoMapper, never()).insert(any(NoticeInfo.class));
    }

    @Test
    void saveBanner_shouldRejectBlankImage() {
        BannerInfo banner = new BannerInfo();
        banner.setTitle("Spring Service Banner");
        banner.setImageUrl("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> contentCommandService.saveBanner(banner));

        assertEquals(ApiErrorCode.REQUEST_INVALID.getCode(), ex.getCode());
        assertEquals("Banner image is required", ex.getMessage());
        verify(bannerInfoMapper, never()).insert(any(BannerInfo.class));
    }

    @Test
    void saveForumCategory_shouldRejectBlankName() {
        ForumCategory category = new ForumCategory();
        category.setName("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> contentCommandService.saveForumCategory(category));

        assertEquals(ApiErrorCode.REQUEST_INVALID.getCode(), ex.getCode());
        assertEquals("Forum category name is required", ex.getMessage());
        verify(forumCategoryMapper, never()).insert(any(ForumCategory.class));
    }

    @Test
    void saveOrUpdateMyPost_shouldRejectBlankContent() {
        mockCurrentUser(9L);
        ForumPost post = new ForumPost();
        post.setTitle("community story");
        post.setCoverImage("https://example.com/post-cover.jpg");
        post.setSummary("story summary");
        post.setCategoryId(7L);
        post.setContent("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> contentCommandService.saveOrUpdateMyPost(post));

        assertEquals(ApiErrorCode.REQUEST_INVALID.getCode(), ex.getCode());
        assertEquals("Post content is required", ex.getMessage());
        verify(forumPostMapper, never()).insert(any(ForumPost.class));
    }

    @Test
    void undoMyPostSubmit_shouldDeleteRecentPendingPost() {
        mockCurrentUser(9L);
        ForumPost post = new ForumPost();
        post.setId(62L);
        post.setUserId(9L);
        post.setStatus("PENDING");
        post.setCreateTime(LocalDateTime.now().minusSeconds(6));
        when(forumPostMapper.selectById(62L)).thenReturn(post);

        contentCommandService.undoMyPostSubmit(62L);

        verify(forumPostMapper).deleteById(62L);
    }

    @Test
    void undoMyPostSubmit_shouldRejectExpiredWindow() {
        mockCurrentUser(9L);
        ForumPost post = new ForumPost();
        post.setId(63L);
        post.setUserId(9L);
        post.setStatus("PENDING");
        post.setCreateTime(LocalDateTime.now().minusSeconds(35));
        when(forumPostMapper.selectById(63L)).thenReturn(post);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> contentCommandService.undoMyPostSubmit(63L)
        );

        assertEquals("Post undo window has expired", ex.getMessage());
        verify(forumPostMapper, never()).deleteById(anyLong());
    }

    @Test
    void deleteForumCategory_shouldRejectWhenStillReferencedByPosts() {
        ForumPost post = new ForumPost();
        post.setId(61L);
        when(forumPostMapper.selectList(any())).thenReturn(java.util.List.of(post));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> contentCommandService.deleteForumCategory(7L)
        );

        assertEquals(ApiErrorCode.BUSINESS_CONFLICT.getCode(), ex.getCode());
        assertEquals("Forum category still has associated posts and cannot be deleted", ex.getMessage());
        verify(forumCategoryMapper, never()).deleteById(anyLong());
    }

    @Test
    void createFavorite_shouldCaptureActivitySnapshotForNewFavorite() {
        mockCurrentUser(9L);
        FavoriteRequest request = new FavoriteRequest();
        request.setActivityId(31L);
        request.setNote("Join later");
        request.setTag("Community care");
        request.setPriority(2);

        Activity activity = new Activity();
        activity.setId(31L);
        activity.setTitle("Senior Companion Service");
        activity.setAddress("Happiness Garden Community Center");
        activity.setStartTime(LocalDateTime.of(2026, 3, 20, 14, 0));
        activity.setEndTime(LocalDateTime.of(2026, 3, 20, 17, 0));
        when(activityMapper.selectById(31L)).thenReturn(activity);
        when(favoriteActivityMapper.selectOne(any())).thenReturn(null);

        contentCommandService.createFavorite(request);

        ArgumentCaptor<FavoriteActivity> favoriteCaptor = ArgumentCaptor.forClass(FavoriteActivity.class);
        verify(favoriteActivityMapper).insert(favoriteCaptor.capture());
        FavoriteActivity saved = favoriteCaptor.getValue();
        assertEquals(9L, saved.getUserId());
        assertEquals(31L, saved.getActivityId());
        assertEquals("Senior Companion Service", saved.getActivityTitle());
        assertEquals("Happiness Garden Community Center", saved.getActivityAddress());
        assertEquals(LocalDateTime.of(2026, 3, 20, 14, 0), saved.getActivityStartTime());
        assertEquals(LocalDateTime.of(2026, 3, 20, 17, 0), saved.getActivityEndTime());
        assertEquals("Join later", saved.getNote());
        assertEquals("Community care", saved.getTag());
        assertEquals(2, saved.getPriority());
    }

    @Test
    void batchArchiveDynamics_shouldUpdateStatusToOffline() {
        contentCommandService.batchArchiveDynamics(java.util.List.of(1L, 2L));

        ArgumentCaptor<UpdateWrapper<InfoDynamic>> wrapperCaptor = ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(infoDynamicMapper).update(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(0));
    }

    @Test
    void batchRestoreNotices_shouldUpdateStatusToPublished() {
        contentCommandService.batchRestoreNotices(java.util.List.of(3L, 4L));

        ArgumentCaptor<UpdateWrapper<NoticeInfo>> wrapperCaptor = ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(noticeInfoMapper).update(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(1));
    }

    @Test
    void deleteDynamic_shouldArchiveInsteadOfPhysicalDelete() {
        InfoDynamic dynamic = new InfoDynamic();
        dynamic.setId(71L);
        dynamic.setTitle("Community Flash");
        dynamic.setStatus(1);
        when(infoDynamicMapper.selectById(71L)).thenReturn(dynamic);

        contentCommandService.deleteDynamic(71L);

        ArgumentCaptor<InfoDynamic> dynamicCaptor = ArgumentCaptor.forClass(InfoDynamic.class);
        verify(infoDynamicMapper).updateById(dynamicCaptor.capture());
        assertEquals(0, dynamicCaptor.getValue().getStatus());
        verify(infoDynamicMapper, never()).deleteById(anyLong());
        verify(adminOperationLogService).record("ARCHIVE_DYNAMIC", "DYNAMIC", 71L, "Community Flash", "delete alias -> archived");
    }

    @Test
    void deleteNotice_shouldArchiveInsteadOfPhysicalDelete() {
        NoticeInfo notice = new NoticeInfo();
        notice.setId(72L);
        notice.setTitle("Window Update");
        notice.setStatus(1);
        when(noticeInfoMapper.selectById(72L)).thenReturn(notice);

        contentCommandService.deleteNotice(72L);

        ArgumentCaptor<NoticeInfo> noticeCaptor = ArgumentCaptor.forClass(NoticeInfo.class);
        verify(noticeInfoMapper).updateById(noticeCaptor.capture());
        assertEquals(0, noticeCaptor.getValue().getStatus());
        verify(noticeInfoMapper, never()).deleteById(anyLong());
        verify(adminOperationLogService).record("ARCHIVE_NOTICE", "NOTICE", 72L, "Window Update", "delete alias -> archived");
    }

    @Test
    void deleteForumPost_shouldArchiveInsteadOfPhysicalDelete() {
        ForumPost post = new ForumPost();
        post.setId(73L);
        post.setTitle("Volunteer Story");
        post.setStatus("APPROVED");
        when(forumPostMapper.selectById(73L)).thenReturn(post);

        contentCommandService.deleteForumPost(73L);

        ArgumentCaptor<ForumPost> postCaptor = ArgumentCaptor.forClass(ForumPost.class);
        verify(forumPostMapper).updateById(postCaptor.capture());
        assertEquals("ARCHIVED", postCaptor.getValue().getStatus());
        verify(forumPostMapper, never()).deleteById(anyLong());
        verify(commentInfoMapper, never()).delete(any());
        verify(adminOperationLogService).record("ARCHIVE_POST", "POST", 73L, "Volunteer Story", "delete alias -> archived");
    }

    @Test
    void createExchangeOrder_shouldDeductPointsAndStockAndCreateLog() {
        mockCurrentUser(9L);

        User user = new User();
        user.setId(9L);
        user.setUsername("volunteer-9");
        user.setRealName("Volunteer Nine");
        user.setPoints(120);
        user.setStatus(1);
        when(userMapper.selectById(9L)).thenReturn(user);

        MallProduct product = new MallProduct();
        product.setId(15L);
        product.setName("Service Badge");
        product.setImageUrl("https://example.com/badge.jpg");
        product.setSummary("Community service badge");
        product.setPointsCost(30);
        product.setStock(8);
        product.setStatus(1);
        when(mallProductMapper.selectById(15L)).thenReturn(product);
        when(exchangeOrderMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            ExchangeOrder order = invocation.getArgument(0);
            order.setId(301L);
            return 1;
        }).when(exchangeOrderMapper).insert(any(ExchangeOrder.class));
        when(userMapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);
        when(mallProductMapper.update(any(), any(UpdateWrapper.class))).thenReturn(1);

        ExchangeOrderCreateRequest request = new ExchangeOrderCreateRequest();
        request.setProductId(15L);
        request.setQuantity(2);
        request.setReceiverName("Volunteer Nine");
        request.setReceiverPhone("13800138000");
        request.setReceiverAddress("Community Service Center");
        request.setRequestKey("req-order-15");

        ExchangeOrder created = contentCommandService.createExchangeOrder(request);

        ArgumentCaptor<ExchangeOrder> orderCaptor = ArgumentCaptor.forClass(ExchangeOrder.class);
        verify(exchangeOrderMapper).insert(orderCaptor.capture());
        verify(userMapper).update(any(), any(UpdateWrapper.class));
        verify(mallProductMapper).update(any(), any(UpdateWrapper.class));
        ArgumentCaptor<PointsChangeLog> logCaptor = ArgumentCaptor.forClass(PointsChangeLog.class);
        verify(pointsChangeLogMapper).insert(logCaptor.capture());

        ExchangeOrder saved = orderCaptor.getValue();
        assertEquals("Service Badge", saved.getProductName());
        assertEquals(2, saved.getQuantity());
        assertEquals(60, saved.getTotalPoints());
        assertEquals("CREATED", saved.getStatus());
        assertEquals("req-order-15", saved.getRequestKey());
        assertEquals(301L, created.getId());
        assertEquals("DEBIT", logCaptor.getValue().getChangeDirection());
        assertEquals(60, logCaptor.getValue().getDeltaPoints());
    }

    @Test
    void createExchangeOrder_shouldReturnExistingOrderForSameRequestKey() {
        mockCurrentUser(9L);

        User user = new User();
        user.setId(9L);
        user.setStatus(1);
        when(userMapper.selectById(9L)).thenReturn(user);

        MallProduct product = new MallProduct();
        product.setId(22L);
        product.setStatus(1);
        when(mallProductMapper.selectById(22L)).thenReturn(product);

        ExchangeOrder existing = new ExchangeOrder();
        existing.setId(900L);
        existing.setOrderNo("ORD202603140001");
        existing.setRequestKey("same-req");
        when(exchangeOrderMapper.selectOne(any())).thenReturn(existing);

        ExchangeOrderCreateRequest request = new ExchangeOrderCreateRequest();
        request.setProductId(22L);
        request.setQuantity(1);
        request.setReceiverName("Tester");
        request.setReceiverPhone("13800138001");
        request.setReceiverAddress("Somewhere");
        request.setRequestKey("same-req");

        ExchangeOrder result = contentCommandService.createExchangeOrder(request);

        assertEquals(900L, result.getId());
        verify(exchangeOrderMapper, never()).insert(any(ExchangeOrder.class));
        verify(pointsChangeLogMapper, never()).insert(any(PointsChangeLog.class));
    }

    @Test
    void updateExchangeOrderStatus_shouldRestorePointsAndStockWhenCancelled() {
        ExchangeOrder order = new ExchangeOrder();
        order.setId(66L);
        order.setUserId(9L);
        order.setProductId(15L);
        order.setQuantity(2);
        order.setTotalPoints(60);
        order.setOrderNo("ORD202603140066");
        order.setStatus("CREATED");
        when(exchangeOrderMapper.selectById(66L)).thenReturn(order);

        ExchangeOrderStatusRequest request = new ExchangeOrderStatusRequest();
        request.setStatus("CANCELLED");
        request.setReason("manual cancel");

        contentCommandService.updateExchangeOrderStatus(66L, request);

        verify(mallProductMapper).update(any(), any(UpdateWrapper.class));
        verify(userMapper).update(any(), any(UpdateWrapper.class));
        verify(pointsChangeLogMapper).insert(any(PointsChangeLog.class));
        ArgumentCaptor<ExchangeOrder> orderCaptor = ArgumentCaptor.forClass(ExchangeOrder.class);
        verify(exchangeOrderMapper).updateById(orderCaptor.capture());
        assertEquals("CANCELLED", orderCaptor.getValue().getStatus());
        assertEquals("manual cancel", orderCaptor.getValue().getStatusReason());
    }

    @Test
    void updateExchangeOrderStatus_shouldRejectIllegalTransition() {
        ExchangeOrder order = new ExchangeOrder();
        order.setId(67L);
        order.setOrderNo("ORD202603140067");
        order.setStatus("CREATED");
        when(exchangeOrderMapper.selectById(67L)).thenReturn(order);

        ExchangeOrderStatusRequest request = new ExchangeOrderStatusRequest();
        request.setStatus("RECEIVED");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> contentCommandService.updateExchangeOrderStatus(67L, request)
        );

        assertEquals(ApiErrorCode.BUSINESS_CONFLICT.getCode(), ex.getCode());
        assertEquals("Order status transition is not allowed", ex.getMessage());
        verify(exchangeOrderMapper, never()).updateById(any(ExchangeOrder.class));
    }

    private void mockCurrentUser(Long userId) {
        Claims claims = mock(Claims.class);
        when(claims.get(SecurityConstants.CLAIM_USER_ID)).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tester", claims)
        );
    }
}
