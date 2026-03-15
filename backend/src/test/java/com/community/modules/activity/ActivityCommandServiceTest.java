package com.community.modules.activity;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.common.constant.SecurityConstants;
import com.community.common.exception.BusinessException;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.dto.SignRequest;
import com.community.modules.activity.dto.ApplicationAuditRequest;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.activity.service.ActivityCommandService;
import com.community.modules.content.mapper.FavoriteActivityMapper;
import com.community.modules.user.entity.User;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityCommandServiceTest {

    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private ActivityApplicationMapper applicationMapper;
    @Mock
    private ActivityCheckRecordMapper checkRecordMapper;
    @Mock
    private FavoriteActivityMapper favoriteActivityMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AdminOperationLogService adminOperationLogService;

    @InjectMocks
    private ActivityCommandService activityCommandService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void batchArchiveActivities_shouldUpdateSelectedActivitiesToArchived() {
        activityCommandService.batchArchiveActivities(java.util.List.of(11L, 12L));

        ArgumentCaptor<UpdateWrapper<Activity>> wrapperCaptor = ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(activityMapper).update(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue("ARCHIVED"));
    }

    @Test
    void batchRestoreActivities_shouldUpdateSelectedActivitiesToPublished() {
        activityCommandService.batchRestoreActivities(java.util.List.of(21L, 22L));

        ArgumentCaptor<UpdateWrapper<Activity>> wrapperCaptor = ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(activityMapper).update(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue("PUBLISHED"));
    }

    @Test
    void applyForActivity_shouldRejectArchivedActivity() {
        mockCurrentUser(9L);
        User user = new User();
        user.setId(9L);
        user.setCertified(1);
        when(userMapper.selectById(9L)).thenReturn(user);

        Activity activity = new Activity();
        activity.setId(31L);
        activity.setStatus("ARCHIVED");
        when(activityMapper.selectById(31L)).thenReturn(activity);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> activityCommandService.applyForActivity(31L, "I can help this weekend")
        );

        assertEquals("Archived activity cannot accept new applications", ex.getMessage());
        verify(applicationMapper, never()).insert(any(ActivityApplication.class));
    }

    @Test
    void undoActivityApplication_shouldDeleteRecentPendingApplication() {
        mockCurrentUser(9L);
        ActivityApplication application = new ActivityApplication();
        application.setId(51L);
        application.setUserId(9L);
        application.setStatus("PENDING");
        application.setCreateTime(LocalDateTime.now().minusSeconds(5));
        when(applicationMapper.selectById(51L)).thenReturn(application);

        activityCommandService.undoActivityApplication(51L);

        verify(applicationMapper).deleteById(51L);
    }

    @Test
    void undoActivityApplication_shouldRejectExpiredWindow() {
        mockCurrentUser(9L);
        ActivityApplication application = new ActivityApplication();
        application.setId(52L);
        application.setUserId(9L);
        application.setStatus("PENDING");
        application.setCreateTime(LocalDateTime.now().minusSeconds(35));
        when(applicationMapper.selectById(52L)).thenReturn(application);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> activityCommandService.undoActivityApplication(52L)
        );

        assertEquals("Application undo window has expired", ex.getMessage());
        verify(applicationMapper, never()).deleteById(anyLong());
    }

    @Test
    void deleteActivity_shouldArchiveInsteadOfPhysicalDelete() {
        Activity activity = new Activity();
        activity.setId(41L);
        activity.setTitle("Weekend Patrol");
        activity.setStatus("PUBLISHED");
        when(activityMapper.selectById(41L)).thenReturn(activity);

        activityCommandService.deleteActivity(41L);

        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityMapper).updateById(activityCaptor.capture());
        assertEquals("ARCHIVED", activityCaptor.getValue().getStatus());
        verify(applicationMapper, never()).delete(any());
        verify(checkRecordMapper, never()).delete(any());
        verify(activityMapper, never()).deleteById(anyLong());
        verify(adminOperationLogService).record(
                "ARCHIVE_ACTIVITY",
                "ACTIVITY",
                41L,
                "Weekend Patrol",
                "delete alias -> archived"
        );
    }

    @Test
    void auditApplication_shouldRejectNonPendingApplication() {
        ActivityApplication application = new ActivityApplication();
        application.setId(61L);
        application.setStatus("APPROVED");
        when(applicationMapper.selectById(61L)).thenReturn(application);

        ApplicationAuditRequest request = new ApplicationAuditRequest();
        request.setStatus("REJECTED");
        request.setRejectReason("duplicate");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> activityCommandService.auditApplication(61L, request)
        );

        assertEquals("Only pending application can be audited", ex.getMessage());
        verify(applicationMapper, never()).updateById(any(ActivityApplication.class));
    }

    @Test
    void signIn_shouldRejectNonApprovedApplication() {
        mockCurrentUser(9L);

        ActivityApplication application = new ActivityApplication();
        application.setId(71L);
        application.setActivityId(31L);
        application.setUserId(9L);
        application.setStatus("PENDING");
        when(applicationMapper.selectById(71L)).thenReturn(application);

        SignRequest request = new SignRequest();
        request.setApplicationId(71L);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> activityCommandService.signIn(request)
        );

        assertEquals("Only approved applications can sign in", ex.getMessage());
        verify(checkRecordMapper, never()).insert(any(com.community.modules.activity.entity.ActivityCheckRecord.class));
        verify(checkRecordMapper, never()).updateById(any(com.community.modules.activity.entity.ActivityCheckRecord.class));
    }

    @Test
    void signOut_shouldRejectWithoutExistingSignIn() {
        mockCurrentUser(9L);

        ActivityApplication application = new ActivityApplication();
        application.setId(72L);
        application.setActivityId(32L);
        application.setUserId(9L);
        application.setStatus("APPROVED");
        when(applicationMapper.selectById(72L)).thenReturn(application);

        Activity activity = new Activity();
        activity.setId(32L);
        activity.setEndTime(java.time.LocalDateTime.now().plusMinutes(10));
        when(activityMapper.selectById(32L)).thenReturn(activity);
        when(checkRecordMapper.selectOne(any())).thenReturn(null);

        SignRequest request = new SignRequest();
        request.setApplicationId(72L);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> activityCommandService.signOut(request)
        );

        assertEquals("Please sign in first", ex.getMessage());
        verify(checkRecordMapper, never()).updateById(any(com.community.modules.activity.entity.ActivityCheckRecord.class));
    }

    private void mockCurrentUser(Long userId) {
        Claims claims = mock(Claims.class);
        when(claims.get(SecurityConstants.CLAIM_USER_ID)).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tester", claims)
        );
    }
}
