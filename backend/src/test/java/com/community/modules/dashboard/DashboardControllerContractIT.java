package com.community.modules.dashboard;

import com.community.common.config.JwtAuthenticationFilter;
import com.community.common.config.SecurityConfig;
import com.community.common.util.JwtTokenUtil;
import com.community.modules.auth.service.DbUserDetailsService;
import com.community.modules.dashboard.controller.DashboardController;
import com.community.modules.dashboard.dto.VolunteerWeeklyRankingItem;
import com.community.modules.dashboard.dto.VolunteerWeeklyRankingResponse;
import com.community.modules.dashboard.service.DashboardService;
import com.community.modules.dashboard.service.VolunteerWeeklyStatsService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DashboardController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class DashboardControllerContractIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private VolunteerWeeklyStatsService volunteerWeeklyStatsService;

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private DbUserDetailsService dbUserDetailsService;

    @Test
    void admin_shouldKeepDashboardContractForAuthenticatedAdmin() throws Exception {
        String token = "dashboard-admin-token";
        mockAuthenticatedAdmin(token, "dashboard-admin");
        when(dashboardService.adminDashboard()).thenReturn(adminPayload());

        mockMvc.perform(get("/api/dashboard/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.activityCount").value(12))
                .andExpect(jsonPath("$.data.postCount").value(8))
                .andExpect(jsonPath("$.data.commentCount").value(15))
                .andExpect(jsonPath("$.data.volunteerCount").value(21))
                .andExpect(jsonPath("$.data.orderCount").value(5))
                .andExpect(jsonPath("$.data.pendingApplicationCount").value(3))
                .andExpect(jsonPath("$.data.pendingPostCount").value(2))
                .andExpect(jsonPath("$.data.weeklyApplicationTrend[0].day").value("2026-03-14"))
                .andExpect(jsonPath("$.data.weeklyApplicationTrend[0].value").value(6))
                .andExpect(jsonPath("$.data.activityTypeBar[0].name").value("Environment Service"))
                .andExpect(jsonPath("$.data.activityTypeBar[0].value").value(4))
                .andExpect(jsonPath("$.data.postTypePie[0].name").value("Community Care"))
                .andExpect(jsonPath("$.data.postTypePie[0].value").value(3))
                .andExpect(jsonPath("$.data.recentOperationLogs[0].operatorUsername").value("dashboard-admin"))
                .andExpect(jsonPath("$.data.recentOperationLogs[0].actionType").value("AUDIT_POST"))
                .andExpect(jsonPath("$.data.recentOperationLogs[0].targetType").value("POST"))
                .andExpect(jsonPath("$.data.recentOperationLogs[0].result").value("SUCCESS"));

        verify(dashboardService).adminDashboard();
    }

    @Test
    void admin_shouldRejectVolunteerRole() throws Exception {
        String token = "dashboard-volunteer-token";
        mockAuthenticatedVolunteer(token, "dashboard-volunteer");

        mockMvc.perform(get("/api/dashboard/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(dashboardService, never()).adminDashboard();
    }

    @Test
    void admin_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100))
                .andExpect(jsonPath("$.message").value("未登录或登录已失效"));

        verify(dashboardService, never()).adminDashboard();
    }

    @Test
    void weeklyRanking_shouldKeepContractForAuthenticatedVolunteer() throws Exception {
        String token = "dashboard-weekly-ranking-token";
        mockAuthenticatedVolunteer(token, "dashboard-volunteer");
        when(volunteerWeeklyStatsService.queryWeeklyRanking(LocalDate.of(2026, 3, 9), 5))
                .thenReturn(weeklyRankingPayload());

        mockMvc.perform(get("/api/dashboard/weekly-ranking")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("weekStart", "2026-03-09")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.weekStart").value("2026-03-09"))
                .andExpect(jsonPath("$.data.weekEnd").value("2026-03-15"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].rankNo").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("volunteer-top"))
                .andExpect(jsonPath("$.data.records[0].serviceMinutes").value(320))
                .andExpect(jsonPath("$.data.records[0].serviceHours").value("5 小时 20 分钟"));

        verify(volunteerWeeklyStatsService).queryWeeklyRanking(LocalDate.of(2026, 3, 9), 5);
    }

    @Test
    void weeklyRanking_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/dashboard/weekly-ranking"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(volunteerWeeklyStatsService, never()).queryWeeklyRanking(null, 20);
    }

    @Test
    void rebuildWeeklyRanking_shouldKeepContractForAuthenticatedAdmin() throws Exception {
        String token = "dashboard-rebuild-admin-token";
        mockAuthenticatedAdmin(token, "dashboard-admin");
        when(volunteerWeeklyStatsService.rebuildWeeklyStats(LocalDate.of(2026, 3, 9), "manual-api")).thenReturn(8);

        mockMvc.perform(post("/api/dashboard/weekly-ranking/rebuild")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("weekStart", "2026-03-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("rebuild success, rows=8"))
                .andExpect(jsonPath("$.data").value((Object) null));

        verify(volunteerWeeklyStatsService).rebuildWeeklyStats(LocalDate.of(2026, 3, 9), "manual-api");
    }

    @Test
    void rebuildWeeklyRanking_shouldRejectVolunteerRole() throws Exception {
        String token = "dashboard-rebuild-volunteer-token";
        mockAuthenticatedVolunteer(token, "dashboard-volunteer");

        mockMvc.perform(post("/api/dashboard/weekly-ranking/rebuild")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(volunteerWeeklyStatsService, never()).rebuildWeeklyStats(null, "manual-api");
    }

    @Test
    void rebuildWeeklyRanking_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/dashboard/weekly-ranking/rebuild"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(volunteerWeeklyStatsService, never()).rebuildWeeklyStats(null, "manual-api");
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

    private Map<String, Object> adminPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("activityCount", 12L);
        payload.put("postCount", 8L);
        payload.put("commentCount", 15L);
        payload.put("volunteerCount", 21L);
        payload.put("orderCount", 5L);
        payload.put("pendingApplicationCount", 3L);
        payload.put("pendingPostCount", 2L);
        payload.put("weeklyApplicationTrend", List.of(Map.of("day", "2026-03-14", "value", 6L)));
        payload.put("activityTypeBar", List.of(Map.of("name", "Environment Service", "value", 4L)));
        payload.put("postTypePie", List.of(Map.of("name", "Community Care", "value", 3L)));
        payload.put("recentOperationLogs", List.of(Map.of(
                "id", 7L,
                "operatorUsername", "dashboard-admin",
                "actionType", "AUDIT_POST",
                "targetType", "POST",
                "targetName", "Volunteer Story",
                "result", "SUCCESS",
                "detail", "status=APPROVED"
        )));
        return payload;
    }

    private VolunteerWeeklyRankingResponse weeklyRankingPayload() {
        return new VolunteerWeeklyRankingResponse(
                LocalDate.of(2026, 3, 9),
                LocalDate.of(2026, 3, 15),
                LocalDateTime.of(2026, 3, 15, 9, 30),
                1,
                List.of(new VolunteerWeeklyRankingItem(
                        1,
                        18L,
                        "volunteer-top",
                        "Volunteer Top",
                        6,
                        6,
                        6,
                        320,
                        "5 小时 20 分钟"
                ))
        );
    }
}
