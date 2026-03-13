package com.community.modules.activity;

import com.community.common.config.JwtAuthenticationFilter;
import com.community.common.config.SecurityConfig;
import com.community.common.util.JwtTokenUtil;
import com.community.common.web.PageResult;
import com.community.modules.activity.controller.ActivityController;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(jsonPath("$.data.volunteerQuota").value(20));

        verify(activityService).detail(31L);
    }

    @Test
    void apply_shouldKeepSubmitContractForAuthenticatedVolunteer() throws Exception {
        String token = "activity-apply-token";
        mockAuthenticatedVolunteer(token, "volunteer-apply");

        mockMvc.perform(post("/api/activity/41/apply")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("applied"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(activityService).applyForActivity(41L);
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

    private PageResult<Activity> pageResult(long current, long pageSize, Activity activity) {
        return new PageResult<>(1, current, pageSize, List.of(activity));
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
}
