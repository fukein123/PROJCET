package com.community.modules.audit;

import com.community.common.config.JwtAuthenticationFilter;
import com.community.common.config.SecurityConfig;
import com.community.common.util.JwtTokenUtil;
import com.community.common.web.PageResult;
import com.community.modules.audit.controller.AdminOperationLogController;
import com.community.modules.audit.entity.AdminOperationLog;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.auth.service.DbUserDetailsService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminOperationLogController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuditControllerContractIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminOperationLogService adminOperationLogService;

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private DbUserDetailsService dbUserDetailsService;

    @Test
    void pageOperations_shouldKeepPageContractForAuthenticatedAdmin() throws Exception {
        String token = "audit-admin-token";
        mockAuthenticatedAdmin(token, "audit-admin");
        when(adminOperationLogService.page(2L, 5L, "AUDIT_APPLICATION", "APPLICATION", "SUCCESS", "audit", "signup"))
                .thenReturn(new PageResult<>(1, 2, 5, List.of(log())));

        mockMvc.perform(get("/api/audit/operations/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "2")
                        .param("size", "5")
                        .param("actionType", "AUDIT_APPLICATION")
                        .param("targetType", "APPLICATION")
                        .param("result", "SUCCESS")
                        .param("operatorKeyword", "audit")
                        .param("targetKeyword", "signup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.records[0].operatorUsername").value("audit-admin"))
                .andExpect(jsonPath("$.data.records[0].actionType").value("AUDIT_APPLICATION"))
                .andExpect(jsonPath("$.data.records[0].targetType").value("APPLICATION"))
                .andExpect(jsonPath("$.data.records[0].targetName").value("Morning Signup"))
                .andExpect(jsonPath("$.data.records[0].result").value("SUCCESS"));

        verify(adminOperationLogService).page(2L, 5L, "AUDIT_APPLICATION", "APPLICATION", "SUCCESS", "audit", "signup");
    }

    @Test
    void pageOperations_shouldRejectVolunteerRole() throws Exception {
        String token = "audit-volunteer-token";
        mockAuthenticatedVolunteer(token, "audit-volunteer");

        mockMvc.perform(get("/api/audit/operations/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(adminOperationLogService, never()).page(1L, 10L, null, null, null, null, null);
    }

    @Test
    void pageOperations_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/audit/operations/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100))
                .andExpect(jsonPath("$.message").value("未登录或登录已失效"));

        verify(adminOperationLogService, never()).page(1L, 10L, null, null, null, null, null);
    }

    private AdminOperationLog log() {
        AdminOperationLog log = new AdminOperationLog();
        log.setId(1L);
        log.setOperatorId(9L);
        log.setOperatorUsername("audit-admin");
        log.setActionType("AUDIT_APPLICATION");
        log.setTargetType("APPLICATION");
        log.setTargetId(18L);
        log.setTargetName("Morning Signup");
        log.setResult("SUCCESS");
        log.setDetail("approved");
        log.setCreateTime(LocalDateTime.of(2026, 3, 14, 15, 30));
        return log;
    }

    private void mockAuthenticatedAdmin(String token, String username) {
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isTokenValid(token)).thenReturn(true);
        when(jwtTokenUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(username);
        when(dbUserDetailsService.loadUserByUsername(username))
                .thenReturn(User.withUsername(username).password("ignored").roles("ADMIN").build());
    }

    private void mockAuthenticatedVolunteer(String token, String username) {
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isTokenValid(token)).thenReturn(true);
        when(jwtTokenUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(username);
        when(dbUserDetailsService.loadUserByUsername(username))
                .thenReturn(User.withUsername(username).password("ignored").roles("VOLUNTEER").build());
    }
}
