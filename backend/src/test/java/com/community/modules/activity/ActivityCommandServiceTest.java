package com.community.modules.activity;

import com.community.common.constant.SecurityConstants;
import com.community.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityApplication;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private ActivityCommandService activityCommandService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void batchArchiveActivities_shouldUpdateSelectedActivitiesToArchived() {
        activityCommandService.batchArchiveActivities(java.util.List.of(11L, 12L));

        ArgumentCaptor<UpdateWrapper<Activity>> wrapperCaptor =
                ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(activityMapper).update(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue("ARCHIVED"));
    }

    @Test
    void batchRestoreActivities_shouldUpdateSelectedActivitiesToPublished() {
        activityCommandService.batchRestoreActivities(java.util.List.of(21L, 22L));

        ArgumentCaptor<UpdateWrapper<Activity>> wrapperCaptor =
                ArgumentCaptor.forClass(UpdateWrapper.class);
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

        BusinessException ex = assertThrows(BusinessException.class, () -> activityCommandService.applyForActivity(31L));

        assertEquals("该活动已归档，暂不可报名", ex.getMessage());
        verify(applicationMapper, never()).insert(any(ActivityApplication.class));
    }

    private void mockCurrentUser(Long userId) {
        Claims claims = mock(Claims.class);
        when(claims.get(SecurityConstants.CLAIM_USER_ID)).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tester", claims)
        );
    }
}
