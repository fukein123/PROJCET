package com.community.modules.user;

import com.community.common.constant.SecurityConstants;
import com.community.common.exception.BusinessException;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.user.dto.UserCreateRequest;
import com.community.modules.user.dto.VolunteerCertificationAuditRequest;
import com.community.modules.user.dto.VolunteerCertificationSubmitRequest;
import com.community.modules.user.entity.User;
import com.community.modules.user.entity.VolunteerCertification;
import com.community.modules.user.mapper.UserMapper;
import com.community.modules.user.mapper.VolunteerCertificationMapper;
import com.community.modules.user.service.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private VolunteerCertificationMapper volunteerCertificationMapper;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AdminOperationLogService adminOperationLogService;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pageUsers_shouldMaskSensitiveFieldsAndHidePassword() {
        User user = new User();
        user.setId(11L);
        user.setUsername("masked-user");
        user.setPhone("13812345678");
        user.setEmail("masked@example.com");
        user.setPassword("secret");
        user.setRole("VOLUNTEER");
        user.setStatus(1);
        VolunteerCertification certification = new VolunteerCertification();
        certification.setId(91L);
        certification.setUserId(11L);
        certification.setStatus("PENDING");
        certification.setIdCardNo("330101199001011234");
        when(userMapper.selectList(any())).thenReturn(List.of(user));
        when(volunteerCertificationMapper.selectList(any())).thenReturn(List.of(certification));

        var result = userService.pageUsers(1, 10, "VOLUNTEER", null, 1, null, "PENDING");

        assertEquals(1, result.getRecords().size());
        assertEquals("138****5678", result.getRecords().getFirst().getPhone());
        assertEquals("m***@example.com", result.getRecords().getFirst().getEmail());
        assertEquals("PENDING", result.getRecords().getFirst().getCertificationStatus());
        assertEquals("3301********1234", result.getRecords().getFirst().getCertificationIdCardNo());
        assertNull(result.getRecords().getFirst().getPassword());
    }

    @Test
    void createUser_shouldCreateAdminWithZeroPointsAndDisabledCertification() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("ops-admin");
        request.setPassword("admin123");
        request.setRealName("Ops Admin");
        request.setPhone("13900001111");
        request.setEmail("ops-admin@example.com");
        request.setRole("ADMIN");
        request.setStatus(1);
        request.setCertified(1);
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-admin123");

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("ADMIN", saved.getRole());
        assertEquals(0, saved.getPoints());
        assertEquals(0, saved.getCertified());
        assertEquals("encoded-admin123", saved.getPassword());
    }

    @Test
    void disableUser_shouldRejectDisablingCurrentLoginUser() {
        mockCurrentUser(8L);
        User current = new User();
        current.setId(8L);
        current.setRole("ADMIN");
        current.setStatus(1);
        when(userMapper.selectById(8L)).thenReturn(current);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.disableUser(8L));

        assertEquals("Current login user cannot be disabled", ex.getMessage());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void submitCurrentUserCertification_shouldAllowApprovedUserToResubmitAndResetCertifiedFlag() {
        mockCurrentUser(18L);
        User user = new User();
        user.setId(18L);
        user.setRole("VOLUNTEER");
        user.setCertified(1);
        user.setStatus(1);
        when(userMapper.selectById(18L)).thenReturn(user);
        when(volunteerCertificationMapper.selectOne(any())).thenReturn(null);

        VolunteerCertificationSubmitRequest request = new VolunteerCertificationSubmitRequest();
        request.setRealName("Volunteer Eighteen");
        request.setIdCardNo("330101199001011234");
        request.setIdCardFrontUrl("https://example.com/id-front.jpg");
        request.setIdCardBackUrl("https://example.com/id-back.jpg");

        userService.submitCurrentUserCertification(request);

        ArgumentCaptor<VolunteerCertification> certificationCaptor = ArgumentCaptor.forClass(VolunteerCertification.class);
        verify(volunteerCertificationMapper).insert(certificationCaptor.capture());
        verify(userMapper).updateById(user);
        assertEquals("PENDING", certificationCaptor.getValue().getStatus());
        assertEquals("330101199001011234", certificationCaptor.getValue().getIdCardNo());
        assertEquals(0, user.getCertified());
    }

    @Test
    void auditUserCertification_shouldApprovePendingCertification() {
        mockCurrentUser(3L);

        User volunteer = new User();
        volunteer.setId(22L);
        volunteer.setRole("VOLUNTEER");
        volunteer.setCertified(0);
        when(userMapper.selectById(22L)).thenReturn(volunteer);

        VolunteerCertification certification = new VolunteerCertification();
        certification.setId(101L);
        certification.setUserId(22L);
        certification.setRealName("Volunteer Twenty Two");
        certification.setStatus("PENDING");
        when(volunteerCertificationMapper.selectOne(any())).thenReturn(certification);

        VolunteerCertificationAuditRequest request = new VolunteerCertificationAuditRequest();
        request.setStatus("APPROVED");

        userService.auditUserCertification(22L, request);

        ArgumentCaptor<VolunteerCertification> certificationCaptor = ArgumentCaptor.forClass(VolunteerCertification.class);
        verify(volunteerCertificationMapper).updateById(certificationCaptor.capture());
        verify(userMapper).updateById(volunteer);
        assertEquals("APPROVED", certificationCaptor.getValue().getStatus());
        assertEquals(1, volunteer.getCertified());
        assertEquals("Volunteer Twenty Two", volunteer.getRealName());
    }

    @Test
    void auditUserCertification_shouldRejectNonPendingCertification() {
        User volunteer = new User();
        volunteer.setId(23L);
        volunteer.setRole("VOLUNTEER");
        volunteer.setCertified(1);
        when(userMapper.selectById(23L)).thenReturn(volunteer);

        VolunteerCertification certification = new VolunteerCertification();
        certification.setId(102L);
        certification.setUserId(23L);
        certification.setStatus("APPROVED");
        when(volunteerCertificationMapper.selectOne(any())).thenReturn(certification);

        VolunteerCertificationAuditRequest request = new VolunteerCertificationAuditRequest();
        request.setStatus("REJECTED");
        request.setRejectReason("duplicate review");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.auditUserCertification(23L, request)
        );

        assertEquals("Only pending certification can be audited", ex.getMessage());
        verify(volunteerCertificationMapper, never()).updateById(any(VolunteerCertification.class));
        verify(userMapper, never()).updateById(volunteer);
    }

    @Test
    void auditUserCertification_shouldRequireRejectReason() {
        User volunteer = new User();
        volunteer.setId(24L);
        volunteer.setRole("VOLUNTEER");
        volunteer.setCertified(0);
        when(userMapper.selectById(24L)).thenReturn(volunteer);

        VolunteerCertification certification = new VolunteerCertification();
        certification.setId(103L);
        certification.setUserId(24L);
        certification.setStatus("PENDING");
        when(volunteerCertificationMapper.selectOne(any())).thenReturn(certification);

        VolunteerCertificationAuditRequest request = new VolunteerCertificationAuditRequest();
        request.setStatus("REJECTED");

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.auditUserCertification(24L, request)
        );

        assertEquals("Reject reason is required", ex.getMessage());
        verify(volunteerCertificationMapper, never()).updateById(any(VolunteerCertification.class));
        verify(userMapper, never()).updateById(volunteer);
    }

    private void mockCurrentUser(Long userId) {
        Claims claims = mock(Claims.class);
        when(claims.get(SecurityConstants.CLAIM_USER_ID)).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tester", claims)
        );
    }
}
