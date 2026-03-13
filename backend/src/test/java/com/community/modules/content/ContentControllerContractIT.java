package com.community.modules.content;

import com.community.common.config.JwtAuthenticationFilter;
import com.community.common.config.SecurityConfig;
import com.community.common.util.JwtTokenUtil;
import com.community.common.web.PageResult;
import com.community.modules.auth.service.DbUserDetailsService;
import com.community.modules.content.dto.FavoriteUpdateRequest;
import com.community.modules.content.entity.BannerInfo;
import com.community.modules.content.controller.ContentController;
import com.community.modules.content.dto.FavoriteRequest;
import com.community.modules.content.entity.CommentInfo;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.entity.ForumCategory;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.entity.InfoDynamic;
import com.community.modules.content.entity.NoticeInfo;
import com.community.modules.content.service.ContentService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ContentController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ContentControllerContractIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContentService contentService;

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private DbUserDetailsService dbUserDetailsService;

    @Test
    void home_shouldKeepAggregateContract() throws Exception {
        when(contentService.listBanners())
                .thenReturn(List.of(banner(41L, "Spring Cleanup", 91L, 1, 1)));
        when(contentService.hotDynamicsTop5())
                .thenReturn(List.of(dynamic(51L, "Volunteer Story", "NEWS", 28, 1)));
        when(contentService.pageNotices(1L, 6L, true))
                .thenReturn(pageResult(1, 6, notice(61L, "Holiday Notice", 1)));
        when(contentService.pageForumPosts(1L, 6L, null, null, false, true))
                .thenReturn(pageResult(1, 6, forumPost(71L, "Neighborhood Support", 8L, 19L, "APPROVED")));

        mockMvc.perform(get("/api/content/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.banners[0].id").value(41))
                .andExpect(jsonPath("$.data.hotDynamics[0].id").value(51))
                .andExpect(jsonPath("$.data.hotDynamics[0].title").value("Volunteer Story"))
                .andExpect(jsonPath("$.data.notices[0].id").value(61))
                .andExpect(jsonPath("$.data.hotPosts[0].id").value(71))
                .andExpect(jsonPath("$.data.hotPosts[0].status").value("APPROVED"));

        verify(contentService).listBanners();
        verify(contentService).hotDynamicsTop5();
        verify(contentService).pageNotices(1L, 6L, true);
        verify(contentService).pageForumPosts(1L, 6L, null, null, false, true);
    }

    @Test
    void pageDynamics_shouldKeepPageContract() throws Exception {
        when(contentService.pageDynamics(2L, 5L, "NEWS", "volunteer", false))
                .thenReturn(pageResult(2, 5, dynamic(81L, "Volunteer Highlights", "NEWS", 105, 1)));

        mockMvc.perform(get("/api/content/dynamics/page")
                        .param("current", "2")
                        .param("size", "5")
                        .param("type", "NEWS")
                        .param("keyword", "volunteer")
                        .param("onlyPublished", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.records[0].id").value(81))
                .andExpect(jsonPath("$.data.records[0].title").value("Volunteer Highlights"))
                .andExpect(jsonPath("$.data.records[0].type").value("NEWS"))
                .andExpect(jsonPath("$.data.records[0].views").value(105));

        verify(contentService).pageDynamics(2L, 5L, "NEWS", "volunteer", false);
    }

    @Test
    void pageNotices_shouldKeepPageContract() throws Exception {
        when(contentService.pageNotices(4L, 3L, false))
                .thenReturn(pageResult(4, 3, notice(91L, "Service Window Change", 0)));

        mockMvc.perform(get("/api/content/notices/page")
                        .param("current", "4")
                        .param("size", "3")
                        .param("onlyPublished", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(4))
                .andExpect(jsonPath("$.data.pageSize").value(3))
                .andExpect(jsonPath("$.data.records[0].id").value(91))
                .andExpect(jsonPath("$.data.records[0].title").value("Service Window Change"))
                .andExpect(jsonPath("$.data.records[0].status").value(0));

        verify(contentService).pageNotices(4L, 3L, false);
    }

    @Test
    void forumCategories_shouldKeepListContract() throws Exception {
        when(contentService.listForumCategories())
                .thenReturn(List.of(forumCategory(12L, "Mutual Support", 2, 1)));

        mockMvc.perform(get("/api/content/forum/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data[0].id").value(12))
                .andExpect(jsonPath("$.data[0].name").value("Mutual Support"))
                .andExpect(jsonPath("$.data[0].sort").value(2))
                .andExpect(jsonPath("$.data[0].status").value(1));

        verify(contentService).listForumCategories();
    }

    @Test
    void pageForumPosts_shouldKeepPageContract() throws Exception {
        when(contentService.pageForumPosts(3L, 4L, "service", "APPROVED", false, true))
                .thenReturn(pageResult(3, 4, forumPost(25L, "Service Notes", 6L, 18L, "APPROVED")));

        mockMvc.perform(get("/api/content/forum/posts/page")
                        .param("current", "3")
                        .param("size", "4")
                        .param("keyword", "service")
                        .param("status", "APPROVED")
                        .param("onlyMine", "false")
                        .param("onlyApproved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(3))
                .andExpect(jsonPath("$.data.pageSize").value(4))
                .andExpect(jsonPath("$.data.records[0].id").value(25))
                .andExpect(jsonPath("$.data.records[0].title").value("Service Notes"))
                .andExpect(jsonPath("$.data.records[0].categoryId").value(6))
                .andExpect(jsonPath("$.data.records[0].status").value("APPROVED"));

        verify(contentService).pageForumPosts(3L, 4L, "service", "APPROVED", false, true);
    }

    @Test
    void myFavorites_shouldKeepListContractForAuthenticatedVolunteer() throws Exception {
        String token = "favorite-list-token";
        mockAuthenticatedVolunteer(token, "favorite-list-user");
        when(contentService.myFavorites())
                .thenReturn(List.of(favorite(33L, 71L, "Follow up", "Long term")));

        mockMvc.perform(get("/api/content/favorites")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data[0].id").value(33))
                .andExpect(jsonPath("$.data[0].activityId").value(71))
                .andExpect(jsonPath("$.data[0].activityTitle").value("Morning Support Visit"))
                .andExpect(jsonPath("$.data[0].activityAddress").value("Lotus Community Center"))
                .andExpect(jsonPath("$.data[0].activityStartTime").exists())
                .andExpect(jsonPath("$.data[0].activityEndTime").exists())
                .andExpect(jsonPath("$.data[0].note").value("Follow up"))
                .andExpect(jsonPath("$.data[0].tag").value("Long term"))
                .andExpect(jsonPath("$.data[0].priority").value(3));

        verify(contentService).myFavorites();
    }

    @Test
    void pageFavorites_shouldKeepPageContractForAuthenticatedVolunteer() throws Exception {
        String token = "favorite-page-token";
        mockAuthenticatedVolunteer(token, "favorite-page-user");
        when(contentService.pageFavorites(2L, 6L))
                .thenReturn(pageResult(2, 6, favorite(34L, 72L, "Priority join", "High rate")));

        mockMvc.perform(get("/api/content/favorites/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "2")
                        .param("size", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(6))
                .andExpect(jsonPath("$.data.records[0].id").value(34))
                .andExpect(jsonPath("$.data.records[0].activityId").value(72))
                .andExpect(jsonPath("$.data.records[0].activityTitle").value("Family Reading Day"))
                .andExpect(jsonPath("$.data.records[0].activityAddress").value("Xinhua Community Hall"))
                .andExpect(jsonPath("$.data.records[0].tag").value("High rate"));

        verify(contentService).pageFavorites(2L, 6L);
    }

    @Test
    void createFavorite_shouldKeepSubmitContractForAuthenticatedVolunteer() throws Exception {
        String token = "favorite-create-token";
        mockAuthenticatedVolunteer(token, "favorite-create-user");

        mockMvc.perform(post("/api/content/favorites")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": 81,
                                  "note": "Weekend join",
                                  "tag": "Weekend",
                                  "priority": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).createFavorite(any(FavoriteRequest.class));
    }

    @Test
    void updateFavorite_shouldKeepUpdateContractForAuthenticatedVolunteer() throws Exception {
        String token = "favorite-update-token";
        mockAuthenticatedVolunteer(token, "favorite-update-user");

        mockMvc.perform(put("/api/content/favorites/18")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "Need transport support",
                                  "tag": "transport",
                                  "priority": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).updateFavorite(any(Long.class), any(FavoriteUpdateRequest.class));
    }

    @Test
    void saveDynamic_shouldKeepCreateContractForAuthenticatedAdmin() throws Exception {
        String token = "dynamic-create-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(post("/api/content/dynamics")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Morning Service Briefing",
                                  "content": "Volunteer group is ready",
                                  "imageUrl": "https://example.com/dynamic.png",
                                  "type": "NEWS",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).saveDynamic(any(InfoDynamic.class));
    }

    @Test
    void batchArchiveDynamics_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "dynamic-archive-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(post("/api/content/dynamics/batch-archive")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [81, 82]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch archived"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchArchiveDynamics(List.of(81L, 82L));
    }

    @Test
    void saveNotice_shouldKeepCreateContractForAuthenticatedAdmin() throws Exception {
        String token = "notice-create-token";
        mockAuthenticatedAdmin(token, "notice-admin");

        mockMvc.perform(post("/api/content/notices")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Maintenance Window",
                                  "content": "Portal services will pause briefly",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).saveNotice(any(NoticeInfo.class));
    }

    @Test
    void batchRestoreNotices_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "notice-restore-token";
        mockAuthenticatedAdmin(token, "notice-admin");

        mockMvc.perform(post("/api/content/notices/batch-restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch restored"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchRestoreNotices(List.of(91L, 92L));
    }

    @Test
    void saveMyPost_shouldKeepSubmitContractForAuthenticatedVolunteer() throws Exception {
        String token = "post-save-token";
        mockAuthenticatedVolunteer(token, "post-author");

        mockMvc.perform(post("/api/content/forum/posts/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Weekend support plan",
                                  "content": "Need three volunteers for supplies",
                                  "categoryId": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("saved"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).saveOrUpdateMyPost(any(ForumPost.class));
    }

    @Test
    void auditPost_shouldKeepAuditContractForAuthenticatedAdmin() throws Exception {
        String token = "post-audit-token";
        mockAuthenticatedAdmin(token, "audit-admin");

        mockMvc.perform(put("/api/content/forum/posts/73/audit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("status", "REJECTED")
                        .param("reason", "duplicate content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("audited"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).auditPost(73L, "REJECTED", "duplicate content");
    }

    @Test
    void addComment_shouldKeepCreateContractForAuthenticatedVolunteerAndAssignTestMarker() throws Exception {
        String token = "comment-create-token";
        mockAuthenticatedVolunteer(token, "comment-author");

        mockMvc.perform(post("/api/content/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-Test-Data-Marker", "smoke:contract-it")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "POST",
                                  "targetId": 51,
                                  "content": "Community feedback"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        ArgumentCaptor<CommentInfo> commentCaptor = ArgumentCaptor.forClass(CommentInfo.class);
        verify(contentService).addComment(commentCaptor.capture());
        Assertions.assertEquals("POST", commentCaptor.getValue().getTargetType());
        Assertions.assertEquals(51L, commentCaptor.getValue().getTargetId());
        Assertions.assertEquals("Community feedback", commentCaptor.getValue().getContent());
        Assertions.assertEquals("smoke:contract-it", commentCaptor.getValue().getTestDataTag());
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

    private ForumCategory forumCategory(Long id, String name, Integer sort, Integer status) {
        ForumCategory category = new ForumCategory();
        category.setId(id);
        category.setName(name);
        category.setSort(sort);
        category.setStatus(status);
        return category;
    }

    private ForumPost forumPost(Long id, String title, Long categoryId, Long userId, String status) {
        ForumPost post = new ForumPost();
        post.setId(id);
        post.setTitle(title);
        post.setContent("Community volunteer experience");
        post.setCategoryId(categoryId);
        post.setUserId(userId);
        post.setStatus(status);
        post.setViews(56);
        post.setAuditReason("approved");
        return post;
    }

    private InfoDynamic dynamic(Long id, String title, String type, Integer views, Integer status) {
        InfoDynamic dynamic = new InfoDynamic();
        dynamic.setId(id);
        dynamic.setTitle(title);
        dynamic.setContent("Community volunteer update");
        dynamic.setImageUrl("https://example.com/dynamic.jpg");
        dynamic.setType(type);
        dynamic.setViews(views);
        dynamic.setStatus(status);
        dynamic.setPublishTime(LocalDateTime.of(2026, 3, 13, 9, 0));
        return dynamic;
    }

    private NoticeInfo notice(Long id, String title, Integer status) {
        NoticeInfo notice = new NoticeInfo();
        notice.setId(id);
        notice.setTitle(title);
        notice.setContent("Notice body");
        notice.setStatus(status);
        notice.setPublishTime(LocalDateTime.of(2026, 3, 13, 10, 30));
        return notice;
    }

    private BannerInfo banner(Long id, String title, Long activityId, Integer sort, Integer status) {
        BannerInfo banner = new BannerInfo();
        banner.setId(id);
        banner.setTitle(title);
        banner.setImageUrl("https://example.com/banner.jpg");
        banner.setActivityId(activityId);
        banner.setSort(sort);
        banner.setStatus(status);
        return banner;
    }

    private FavoriteActivity favorite(Long id, Long activityId, String note, String tag) {
        FavoriteActivity favorite = new FavoriteActivity();
        favorite.setId(id);
        favorite.setActivityId(activityId);
        favorite.setUserId(9L);
        favorite.setActivityTitle(activityId.equals(71L) ? "Morning Support Visit" : "Family Reading Day");
        favorite.setActivityAddress(activityId.equals(71L) ? "Lotus Community Center" : "Xinhua Community Hall");
        favorite.setActivityStartTime(LocalDateTime.of(2026, 3, 14, 9, 0));
        favorite.setActivityEndTime(LocalDateTime.of(2026, 3, 14, 11, 30));
        favorite.setNote(note);
        favorite.setTag(tag);
        favorite.setPriority(3);
        return favorite;
    }
}
