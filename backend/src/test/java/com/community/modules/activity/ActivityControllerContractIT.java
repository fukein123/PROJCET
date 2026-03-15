package com.community.modules.activity;

import com.community.common.config.JwtAuthenticationFilter;
import com.community.common.config.SecurityConfig;
import com.community.common.util.JwtTokenUtil;
import com.community.common.web.PageResult;
import com.community.modules.activity.controller.ActivityController;
import com.community.modules.activity.dto.ActivityApplicationView;
import com.community.modules.activity.dto.CheckRecordView;
import com.community.modules.activity.dto.SignRequest;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityCategory;
import com.community.modules.activity.service.ActivityService;
import com.community.modules.auth.service.DbUserDetailsService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ActivityController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ActivityControllerContractIT {

    private static final String ACTIVITY_PAGE_API = "/api/activity/page";
    private static final String ACTIVITY_CATEGORIES_API = "/api/activity/categories";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private DbUserDetailsService dbUserDetailsService;

    @Test
    void categories_shouldKeepListContract() throws Exception {
        when(activityService.listCategories())
                .thenReturn(List.of(category(11L, "Environment Service", "Community cleanup", 1, 1)));

        mockMvc.perform(get(ACTIVITY_CATEGORIES_API))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data[0].id").value(11))
                .andExpect(jsonPath("$.data[0].name").value("Environment Service"))
                .andExpect(jsonPath("$.data[0].description").value("Community cleanup"))
                .andExpect(jsonPath("$.data[0].sort").value(1))
                .andExpect(jsonPath("$.data[0].status").value(1));

        verify(activityService).listCategories();
    }

    @Test
    void page_shouldKeepPageContract() throws Exception {
        when(activityService.pageActivities(2L, 5L, "patrol", 3L, "PUBLISHED", false))
                .thenReturn(pageResult(2, 5, activity(21L, "Night Patrol", 3L, "PUBLISHED")));

        mockMvc.perform(get(ACTIVITY_PAGE_API)
                        .param("current", "2")
                        .param("size", "5")
                        .param("keyword", "patrol")
                        .param("categoryId", "3")
                        .param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.records[0].id").value(21))
                .andExpect(jsonPath("$.data.records[0].title").value("Night Patrol"))
                .andExpect(jsonPath("$.data.records[0].categoryId").value(3))
                .andExpect(jsonPath("$.data.records[0].status").value("PUBLISHED"));

        verify(activityService).pageActivities(2L, 5L, "patrol", 3L, "PUBLISHED", false);
    }

    @Test
    void detail_shouldKeepDetailContractForAuthenticatedVolunteer() throws Exception {
        String token = "activity-detail-token";
        mockAuthenticatedVolunteer(token, "volunteer-detail");
        when(activityService.detail(31L)).thenReturn(activity(31L, "Senior Visit", 5L, "ONGOING"));

        mockMvc.perform(get("/api/activity/31")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(31))
                .andExpect(jsonPath("$.data.title").value("Senior Visit"))
                .andExpect(jsonPath("$.data.categoryId").value(5))
                .andExpect(jsonPath("$.data.status").value("ONGOING"))
                .andExpect(jsonPath("$.data.targetCount").value(30))
                .andExpect(jsonPath("$.data.volunteerQuota").value(20))
                .andExpect(jsonPath("$.data.pointReward").value(16));

        verify(activityService).detail(31L);
    }

    @Test
    void apply_shouldKeepSubmitContractForAuthenticatedVolunteer() throws Exception {
        String token = "activity-apply-token";
        mockAuthenticatedVolunteer(token, "volunteer-apply");
        com.community.modules.activity.entity.ActivityApplication application = new com.community.modules.activity.entity.ActivityApplication();
        application.setId(401L);
        application.setActivityId(41L);
        application.setUserId(9L);
        application.setStatus("PENDING");
        application.setApplyReason("Weekend service is available");
        when(activityService.applyForActivity(41L, "Weekend service is available")).thenReturn(application);

        mockMvc.perform(post("/api/activity/41/apply")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applyReason": "Weekend service is available"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("applied"))
                .andExpect(jsonPath("$.data.id").value(401))
                .andExpect(jsonPath("$.data.activityId").value(41))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        verify(activityService).applyForActivity(41L, "Weekend service is available");
    }

    @Test
    void apply_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "activity-apply-admin-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(post("/api/activity/41/apply")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applyReason": "Admin account should be blocked"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(activityService, never()).applyForActivity(41L, "Admin account should be blocked");
    }

    @Test
    void apply_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/activity/41/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applyReason": "Anonymous user should be blocked"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).applyForActivity(41L, "Anonymous user should be blocked");
    }

    @Test
    void undoApplication_shouldKeepDeleteContractForAuthenticatedVolunteer() throws Exception {
        String token = "activity-undo-token";
        mockAuthenticatedVolunteer(token, "volunteer-undo");

        mockMvc.perform(delete("/api/activity/applications/41/undo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("undone"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(activityService).undoActivityApplication(41L);
    }

    @Test
    void undoApplication_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "activity-undo-admin-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(delete("/api/activity/applications/41/undo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(activityService, never()).undoActivityApplication(41L);
    }

    @Test
    void undoApplication_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/activity/applications/41/undo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).undoActivityApplication(41L);
    }

    @Test
    void myApplications_shouldKeepPageContractForAuthenticatedVolunteer() throws Exception {
        String token = "activity-my-applications-token";
        mockAuthenticatedVolunteer(token, "volunteer-records");
        when(activityService.pageApplications(2L, 6L, 41L, "PENDING", true))
                .thenReturn(pageResult(2, 6, applicationView(501L, 41L, 9L, "PENDING")));

        mockMvc.perform(get("/api/activity/applications/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "2")
                        .param("size", "6")
                        .param("activityId", "41")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(6))
                .andExpect(jsonPath("$.data.records[0].id").value(501))
                .andExpect(jsonPath("$.data.records[0].activityId").value(41))
                .andExpect(jsonPath("$.data.records[0].userId").value(9))
                .andExpect(jsonPath("$.data.records[0].status").value("PENDING"));

        verify(activityService).pageApplications(2L, 6L, 41L, "PENDING", true);
    }

    @Test
    void myApplications_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "activity-my-applications-admin-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(get("/api/activity/applications/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(activityService, never()).pageApplications(1L, 10L, null, null, true);
    }

    @Test
    void myApplications_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/activity/applications/my"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).pageApplications(1L, 10L, null, null, true);
    }

    @Test
    void batchArchive_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "activity-archive-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(post("/api/activity/batch-archive")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [41, 42]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch archived"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(activityService).batchArchiveActivities(List.of(41L, 42L));
    }

    @Test
    void batchArchive_shouldRejectVolunteerRole() throws Exception {
        String token = "activity-archive-volunteer-token";
        mockAuthenticatedVolunteer(token, "activity-volunteer");

        mockMvc.perform(post("/api/activity/batch-archive")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [41, 42]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(activityService, never()).batchArchiveActivities(List.of(41L, 42L));
    }

    @Test
    void batchArchive_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/activity/batch-archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [41, 42]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).batchArchiveActivities(List.of(41L, 42L));
    }

    @Test
    void batchRestore_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "activity-restore-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(post("/api/activity/batch-restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [51, 52]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch restored"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(activityService).batchRestoreActivities(List.of(51L, 52L));
    }

    @Test
    void batchRestore_shouldRejectVolunteerRole() throws Exception {
        String token = "activity-restore-volunteer-token";
        mockAuthenticatedVolunteer(token, "activity-volunteer");

        mockMvc.perform(post("/api/activity/batch-restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [51, 52]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(activityService, never()).batchRestoreActivities(List.of(51L, 52L));
    }

    @Test
    void batchRestore_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/activity/batch-restore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [51, 52]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).batchRestoreActivities(List.of(51L, 52L));
    }

    @Test
    void pageCheckRecords_shouldKeepPageContractForAuthenticatedAdmin() throws Exception {
        String token = "check-record-page-token";
        mockAuthenticatedAdmin(token, "activity-admin");
        when(activityService.pageCheckRecords(2L, 5L, 31L, 91L, "FINISHED", "alice"))
                .thenReturn(pageResult(2, 5, checkRecord(61L, 31L, 91L, "alice.volunteer", "Alice", "FINISHED")));

        mockMvc.perform(get("/api/activity/sign/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "2")
                        .param("size", "5")
                        .param("activityId", "31")
                        .param("userId", "91")
                        .param("status", "FINISHED")
                        .param("keyword", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.records[0].id").value(61))
                .andExpect(jsonPath("$.data.records[0].activityId").value(31))
                .andExpect(jsonPath("$.data.records[0].activityTitle").value("Senior Visit"))
                .andExpect(jsonPath("$.data.records[0].userId").value(91))
                .andExpect(jsonPath("$.data.records[0].username").value("alice.volunteer"))
                .andExpect(jsonPath("$.data.records[0].realName").value("Alice"))
                .andExpect(jsonPath("$.data.records[0].status").value("FINISHED"))
                .andExpect(jsonPath("$.data.records[0].serviceMinutes").value(95));

        verify(activityService).pageCheckRecords(2L, 5L, 31L, 91L, "FINISHED", "alice");
    }

    @Test
    void pageCheckRecords_shouldRejectVolunteerRole() throws Exception {
        String token = "check-record-page-volunteer-token";
        mockAuthenticatedVolunteer(token, "activity-volunteer");

        mockMvc.perform(get("/api/activity/sign/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(activityService, never()).pageCheckRecords(1L, 10L, null, null, null, null);
    }

    @Test
    void pageCheckRecords_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/activity/sign/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).pageCheckRecords(1L, 10L, null, null, null, null);
    }

    @Test
    void signIn_shouldKeepSubmitContractForAuthenticatedVolunteer() throws Exception {
        String token = "activity-sign-in-token";
        mockAuthenticatedVolunteer(token, "sign-in-volunteer");

        mockMvc.perform(post("/api/activity/sign/in")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": 601
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("sign-in success"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(activityService).signIn(signRequestWithApplicationId(601L));
    }

    @Test
    void signIn_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "activity-sign-in-admin-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(post("/api/activity/sign/in")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": 601
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(activityService, never()).signIn(signRequestWithApplicationId(601L));
    }

    @Test
    void signIn_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/activity/sign/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": 601
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).signIn(signRequestWithApplicationId(601L));
    }

    @Test
    void signOut_shouldKeepSubmitContractForAuthenticatedVolunteer() throws Exception {
        String token = "activity-sign-out-token";
        mockAuthenticatedVolunteer(token, "sign-out-volunteer");

        mockMvc.perform(post("/api/activity/sign/out")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": 602
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("sign-out success"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(activityService).signOut(signRequestWithApplicationId(602L));
    }

    @Test
    void signOut_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "activity-sign-out-admin-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(post("/api/activity/sign/out")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": 602
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(activityService, never()).signOut(signRequestWithApplicationId(602L));
    }

    @Test
    void signOut_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/activity/sign/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": 602
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).signOut(signRequestWithApplicationId(602L));
    }

    @Test
    void myRecords_shouldKeepPageContractForAuthenticatedVolunteer() throws Exception {
        String token = "activity-my-records-token";
        mockAuthenticatedVolunteer(token, "my-records-volunteer");
        when(activityService.myCheckRecords(3L, 4L))
                .thenReturn(pageResult(3, 4, checkRecord(62L, 31L, 9L, "records.volunteer", "Records User", "SIGNED_IN")));

        mockMvc.perform(get("/api/activity/sign/my-records")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "3")
                        .param("size", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(3))
                .andExpect(jsonPath("$.data.pageSize").value(4))
                .andExpect(jsonPath("$.data.records[0].id").value(62))
                .andExpect(jsonPath("$.data.records[0].userId").value(9))
                .andExpect(jsonPath("$.data.records[0].status").value("SIGNED_IN"));

        verify(activityService).myCheckRecords(3L, 4L);
    }

    @Test
    void myRecords_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "activity-my-records-admin-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(get("/api/activity/sign/my-records")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(activityService, never()).myCheckRecords(1L, 10L);
    }

    @Test
    void pageApplications_shouldKeepPageContractForAuthenticatedAdmin() throws Exception {
        String token = "activity-page-applications-admin-token";
        mockAuthenticatedAdmin(token, "activity-admin");
        when(activityService.pageApplications(3L, 4L, 71L, "APPROVED", false))
                .thenReturn(pageResult(3, 4, applicationView(701L, 71L, 9L, "APPROVED")));

        mockMvc.perform(get("/api/activity/applications/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "3")
                        .param("size", "4")
                        .param("activityId", "71")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.current").value(3))
                .andExpect(jsonPath("$.data.pageSize").value(4))
                .andExpect(jsonPath("$.data.records[0].id").value(701))
                .andExpect(jsonPath("$.data.records[0].activityId").value(71))
                .andExpect(jsonPath("$.data.records[0].status").value("APPROVED"));

        verify(activityService).pageApplications(3L, 4L, 71L, "APPROVED", false);
    }

    @Test
    void pageApplications_shouldRejectVolunteerRole() throws Exception {
        String token = "activity-page-applications-volunteer-token";
        mockAuthenticatedVolunteer(token, "activity-volunteer");

        mockMvc.perform(get("/api/activity/applications/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(activityService, never()).pageApplications(1L, 10L, null, null, false);
    }

    @Test
    void pageApplications_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/activity/applications/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).pageApplications(1L, 10L, null, null, false);
    }

    @Test
    void audit_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "activity-audit-admin-token";
        mockAuthenticatedAdmin(token, "activity-admin");

        mockMvc.perform(put("/api/activity/applications/71/audit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("audited"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(activityService).auditApplication(eq(71L), any());
    }

    @Test
    void audit_shouldRejectVolunteerRole() throws Exception {
        String token = "activity-audit-volunteer-token";
        mockAuthenticatedVolunteer(token, "activity-volunteer");

        mockMvc.perform(put("/api/activity/applications/71/audit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(activityService, never()).auditApplication(eq(71L), any());
    }

    @Test
    void audit_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/activity/applications/71/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).auditApplication(eq(71L), any());
    }

    @Test
    void myRecords_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/activity/sign/my-records"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(activityService, never()).myCheckRecords(1L, 10L);
    }

    private void mockAuthenticatedVolunteer(String token, String username) {
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isTokenValid(token)).thenReturn(true);
        when(jwtTokenUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(username);
        when(dbUserDetailsService.loadUserByUsername(username))
                .thenReturn(User.withUsername(username).password("ignored").roles("VOLUNTEER").build());
    }

    private void mockAuthenticatedAdmin(String token, String username) {
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isTokenValid(token)).thenReturn(true);
        when(jwtTokenUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(username);
        when(dbUserDetailsService.loadUserByUsername(username))
                .thenReturn(User.withUsername(username).password("ignored").roles("ADMIN").build());
    }

    private <T> PageResult<T> pageResult(long current, long pageSize, T record) {
        return new PageResult<>(1, current, pageSize, List.of(record));
    }

    private Activity activity(Long id, String title, Long categoryId, String status) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setTitle(title);
        activity.setCategoryId(categoryId);
        activity.setStatus(status);
        activity.setAddress("Community Station");
        activity.setTargetCount(30);
        activity.setVolunteerQuota(20);
        activity.setPointReward(16);
        activity.setContent("Patrol task details");
        activity.setDescription("Building patrol and record keeping");
        activity.setStartTime(LocalDateTime.of(2026, 3, 15, 9, 0));
        activity.setEndTime(LocalDateTime.of(2026, 3, 15, 11, 0));
        return activity;
    }

    private ActivityCategory category(Long id, String name, String description, Integer sort, Integer status) {
        ActivityCategory category = new ActivityCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setSort(sort);
        category.setStatus(status);
        return category;
    }

    private CheckRecordView checkRecord(Long id,
                                        Long activityId,
                                        Long userId,
                                        String username,
                                        String realName,
                                        String status) {
        CheckRecordView record = new CheckRecordView();
        record.setId(id);
        record.setActivityId(activityId);
        record.setActivityTitle("Senior Visit");
        record.setActivityAddress("Community Station");
        record.setActivityStartTime(LocalDateTime.of(2026, 3, 15, 9, 0));
        record.setActivityEndTime(LocalDateTime.of(2026, 3, 15, 11, 0));
        record.setUserId(userId);
        record.setUsername(username);
        record.setRealName(realName);
        record.setSignInTime(LocalDateTime.of(2026, 3, 15, 9, 5));
        record.setSignOutTime(LocalDateTime.of(2026, 3, 15, 10, 40));
        record.setSignInDistance(12.5);
        record.setSignOutDistance(8.0);
        record.setStatus(status);
        record.setServiceMinutes(95L);
        return record;
    }

    private ActivityApplicationView applicationView(Long id, Long activityId, Long userId, String status) {
        ActivityApplicationView view = new ActivityApplicationView();
        view.setId(id);
        view.setActivityId(activityId);
        view.setActivityTitle("Weekend Support");
        view.setActivityStartTime(LocalDateTime.of(2026, 3, 16, 9, 0));
        view.setActivityEndTime(LocalDateTime.of(2026, 3, 16, 11, 30));
        view.setUserId(userId);
        view.setUsername("records.volunteer");
        view.setRealName("Records User");
        view.setApplyReason("Can join in the morning");
        view.setStatus(status);
        view.setApplyTime(LocalDateTime.of(2026, 3, 15, 18, 0));
        return view;
    }

    private SignRequest signRequestWithApplicationId(Long applicationId) {
        SignRequest request = new SignRequest();
        request.setApplicationId(applicationId);
        return request;
    }
}
