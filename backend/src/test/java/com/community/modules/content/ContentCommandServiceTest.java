package com.community.modules.content;

import com.community.common.constant.SecurityConstants;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.common.exception.ApiErrorCode;
import com.community.common.exception.BusinessException;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.content.dto.FavoriteRequest;
import com.community.modules.content.entity.CommentInfo;
import com.community.modules.content.entity.FavoriteActivity;
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
import com.community.modules.content.service.ContentCommandService;
import com.community.modules.content.service.ForumModerationService;
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
import static org.mockito.Mockito.mock;
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
        request.setNote("周末可参加");
        request.setTag("高优先");
        request.setPriority(7);

        Activity activity = new Activity();
        activity.setId(21L);
        activity.setTitle("周末巡河行动");
        activity.setAddress("滨河社区服务站");
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
        assertEquals("周末可参加", updated.getNote());
        assertEquals("高优先", updated.getTag());
        assertEquals(5, updated.getPriority());
        assertEquals("周末巡河行动", updated.getActivityTitle());
        assertEquals("滨河社区服务站", updated.getActivityAddress());
        assertEquals(LocalDateTime.of(2026, 3, 16, 9, 0), updated.getActivityStartTime());
        assertEquals(LocalDateTime.of(2026, 3, 16, 11, 0), updated.getActivityEndTime());
    }

    @Test
    void saveOrUpdateMyPost_shouldRejectEditingOthersPost() {
        mockCurrentUser(9L);
        ForumPost request = new ForumPost();
        request.setId(31L);
        request.setTitle("更新后的帖子");
        request.setContent("内容");
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
        assertEquals("不能编辑他人的帖子", ex.getMessage());
        verify(forumPostMapper, never()).updateById(any(ForumPost.class));
    }

    @Test
    void addComment_shouldRejectUnsupportedTargetType() {
        CommentInfo comment = new CommentInfo();
        comment.setTargetType("NOTICE");
        comment.setTargetId(45L);
        comment.setContent("围观一下");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> contentCommandService.addComment(comment)
        );

        assertEquals("不支持的评论目标类型", ex.getMessage());
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
        comment.setContent("  活动反馈很好  ");
        comment.setTestDataTag("smoke:add-comment");

        contentCommandService.addComment(comment);

        ArgumentCaptor<CommentInfo> commentCaptor = ArgumentCaptor.forClass(CommentInfo.class);
        verify(commentInfoMapper).insert(commentCaptor.capture());
        CommentInfo saved = commentCaptor.getValue();
        assertEquals(9L, saved.getUserId());
        assertEquals("活动反馈很好", saved.getContent());
        assertEquals(1, saved.getStatus());
        assertEquals("smoke:add-comment", saved.getTestDataTag());
    }

    @Test
    void saveDynamic_shouldRejectBlankTitle() {
        InfoDynamic dynamic = new InfoDynamic();
        dynamic.setTitle("   ");
        dynamic.setContent("valid");

        BusinessException ex = assertThrows(BusinessException.class, () -> contentCommandService.saveDynamic(dynamic));

        assertEquals(ApiErrorCode.REQUEST_INVALID.getCode(), ex.getCode());
        assertEquals("资讯动态标题不能为空", ex.getMessage());
        verify(infoDynamicMapper, never()).insert(any(InfoDynamic.class));
    }

    @Test
    void saveNotice_shouldRejectBlankContent() {
        NoticeInfo notice = new NoticeInfo();
        notice.setTitle("notice");
        notice.setContent("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> contentCommandService.saveNotice(notice));

        assertEquals(ApiErrorCode.REQUEST_INVALID.getCode(), ex.getCode());
        assertEquals("公告内容不能为空", ex.getMessage());
        verify(noticeInfoMapper, never()).insert(any(NoticeInfo.class));
    }

    @Test
    void saveOrUpdateMyPost_shouldRejectBlankContent() {
        mockCurrentUser(9L);
        ForumPost post = new ForumPost();
        post.setTitle("community story");
        post.setCategoryId(7L);
        post.setContent("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> contentCommandService.saveOrUpdateMyPost(post));

        assertEquals(ApiErrorCode.REQUEST_INVALID.getCode(), ex.getCode());
        assertEquals("帖子内容不能为空", ex.getMessage());
        verify(forumPostMapper, never()).insert(any(ForumPost.class));
    }

    @Test
    void createFavorite_shouldCaptureActivitySnapshotForNewFavorite() {
        mockCurrentUser(9L);
        FavoriteRequest request = new FavoriteRequest();
        request.setActivityId(31L);
        request.setNote("想后续持续参与");
        request.setTag("社区关爱");
        request.setPriority(2);

        Activity activity = new Activity();
        activity.setId(31L);
        activity.setTitle("社区老人陪伴服务");
        activity.setAddress("幸福家园社区中心");
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
        assertEquals("社区老人陪伴服务", saved.getActivityTitle());
        assertEquals("幸福家园社区中心", saved.getActivityAddress());
        assertEquals(LocalDateTime.of(2026, 3, 20, 14, 0), saved.getActivityStartTime());
        assertEquals(LocalDateTime.of(2026, 3, 20, 17, 0), saved.getActivityEndTime());
        assertEquals("想后续持续参与", saved.getNote());
        assertEquals("社区关爱", saved.getTag());
        assertEquals(2, saved.getPriority());
    }

    @Test
    void batchArchiveDynamics_shouldUpdateStatusToOffline() {
        contentCommandService.batchArchiveDynamics(java.util.List.of(1L, 2L));

        ArgumentCaptor<UpdateWrapper<InfoDynamic>> wrapperCaptor =
                ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(infoDynamicMapper).update(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(0));
    }

    @Test
    void batchRestoreNotices_shouldUpdateStatusToPublished() {
        contentCommandService.batchRestoreNotices(java.util.List.of(3L, 4L));

        ArgumentCaptor<UpdateWrapper<NoticeInfo>> wrapperCaptor =
                ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(noticeInfoMapper).update(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(1));
    }

    private void mockCurrentUser(Long userId) {
        Claims claims = mock(Claims.class);
        when(claims.get(SecurityConstants.CLAIM_USER_ID)).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tester", claims)
        );
    }
}
