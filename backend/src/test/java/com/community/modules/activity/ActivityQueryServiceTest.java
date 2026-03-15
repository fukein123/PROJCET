package com.community.modules.activity;

import com.community.common.constant.SecurityConstants;
import com.community.common.exception.BusinessException;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.activity.service.ActivityQueryService;
import com.community.modules.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityQueryServiceTest {

    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private ActivityApplicationMapper applicationMapper;
    @Mock
    private ActivityCheckRecordMapper checkRecordMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ActivityQueryService activityQueryService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void detail_shouldHideArchivedActivityForNonAdmin() {
        Activity activity = new Activity();
        activity.setId(51L);
        activity.setStatus("ARCHIVED");
        when(activityMapper.selectById(51L)).thenReturn(activity);

        BusinessException ex = assertThrows(BusinessException.class, () -> activityQueryService.detail(51L));

        assertEquals(40400, ex.getCode());
        assertEquals("Activity not found", ex.getMessage());
    }

    @Test
    void detail_shouldAllowArchivedActivityForAdmin() {
        mockCurrentRole("ADMIN");
        Activity activity = new Activity();
        activity.setId(52L);
        activity.setStatus("ARCHIVED");
        when(activityMapper.selectById(52L)).thenReturn(activity);

        Activity result = activityQueryService.detail(52L);

        assertSame(activity, result);
    }

    private void mockCurrentRole(String role) {
        Claims claims = mock(Claims.class);
        when(claims.get(SecurityConstants.CLAIM_ROLE)).thenReturn(role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tester", claims)
        );
    }
}
