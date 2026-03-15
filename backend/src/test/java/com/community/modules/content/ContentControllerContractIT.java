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
import com.community.modules.content.entity.ExchangeOrder;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.entity.ForumCategory;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.entity.InfoDynamic;
import com.community.modules.content.entity.MallProduct;
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
                .andExpect(jsonPath("$.data.records[0].source").value("Community Center"))
                .andExpect(jsonPath("$.data.records[0].type").value("NEWS"))
                .andExpect(jsonPath("$.data.records[0].views").value(105));

        verify(contentService).pageDynamics(2L, 5L, "NEWS", "volunteer", false);
    }

    @Test
    void detailDynamic_shouldKeepDetailContract() throws Exception {
        when(contentService.detailDynamic(82L))
                .thenReturn(dynamic(82L, "Community Brief", "DYNAMIC", 16, 1));

        mockMvc.perform(get("/api/content/dynamics/82"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(82))
                .andExpect(jsonPath("$.data.title").value("Community Brief"))
                .andExpect(jsonPath("$.data.source").value("Community Center"))
                .andExpect(jsonPath("$.data.views").value(16));

        verify(contentService).detailDynamic(82L);
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
    void detailNotice_shouldKeepDetailContract() throws Exception {
        when(contentService.detailNotice(92L))
                .thenReturn(notice(92L, "Volunteer Shift Reminder", 1));

        mockMvc.perform(get("/api/content/notices/92"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(92))
                .andExpect(jsonPath("$.data.title").value("Volunteer Shift Reminder"))
                .andExpect(jsonPath("$.data.content").value("Notice body"));

        verify(contentService).detailNotice(92L);
    }

    @Test
    void adminBanners_shouldKeepListContractForAuthenticatedAdmin() throws Exception {
        String token = "banner-admin-token";
        mockAuthenticatedAdmin(token, "banner-admin");
        when(contentService.listBannersForAdmin())
                .thenReturn(List.of(banner(11L, "Spring Cleanup", 91L, 1, 0)));

        mockMvc.perform(get("/api/content/admin/banners")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data[0].id").value(11))
                .andExpect(jsonPath("$.data[0].title").value("Spring Cleanup"))
                .andExpect(jsonPath("$.data[0].imageUrl").value("https://example.com/banner.jpg"))
                .andExpect(jsonPath("$.data[0].activityId").value(91))
                .andExpect(jsonPath("$.data[0].status").value(0));

        verify(contentService).listBannersForAdmin();
    }

    @Test
    void adminBanners_shouldRejectVolunteerRole() throws Exception {
        String token = "banner-admin-volunteer-token";
        mockAuthenticatedVolunteer(token, "banner-volunteer");

        mockMvc.perform(get("/api/content/admin/banners")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).listBannersForAdmin();
    }

    @Test
    void adminBanners_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/content/admin/banners"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).listBannersForAdmin();
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
    void adminForumCategories_shouldKeepListContractForAuthenticatedAdmin() throws Exception {
        String token = "forum-category-admin-token";
        mockAuthenticatedAdmin(token, "forum-category-admin");
        when(contentService.listForumCategoriesForAdmin())
                .thenReturn(List.of(forumCategory(13L, "Volunteer Stories", 4, 0)));

        mockMvc.perform(get("/api/content/admin/forum/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data[0].id").value(13))
                .andExpect(jsonPath("$.data[0].name").value("Volunteer Stories"))
                .andExpect(jsonPath("$.data[0].sort").value(4))
                .andExpect(jsonPath("$.data[0].status").value(0));

        verify(contentService).listForumCategoriesForAdmin();
    }

    @Test
    void adminForumCategories_shouldRejectVolunteerRole() throws Exception {
        String token = "forum-category-volunteer-token";
        mockAuthenticatedVolunteer(token, "forum-category-volunteer");

        mockMvc.perform(get("/api/content/admin/forum/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).listForumCategoriesForAdmin();
    }

    @Test
    void adminForumCategories_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/content/admin/forum/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).listForumCategoriesForAdmin();
    }

    @Test
    void saveBanner_shouldKeepCreateContractForAuthenticatedAdmin() throws Exception {
        String token = "banner-create-token";
        mockAuthenticatedAdmin(token, "banner-admin");

        mockMvc.perform(post("/api/content/banners")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New Banner",
                                  "imageUrl": "https://example.com/banner-create.jpg",
                                  "activityId": 91,
                                  "sort": 2,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).saveBanner(any(BannerInfo.class));
    }

    @Test
    void saveBanner_shouldRejectVolunteerRole() throws Exception {
        String token = "banner-create-volunteer-token";
        mockAuthenticatedVolunteer(token, "banner-volunteer");

        mockMvc.perform(post("/api/content/banners")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volunteer Banner",
                                  "imageUrl": "https://example.com/banner-create.jpg",
                                  "activityId": 91,
                                  "sort": 2,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).saveBanner(any(BannerInfo.class));
    }

    @Test
    void saveBanner_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous Banner",
                                  "imageUrl": "https://example.com/banner-create.jpg",
                                  "activityId": 91,
                                  "sort": 2,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).saveBanner(any(BannerInfo.class));
    }

    @Test
    void updateBanner_shouldKeepUpdateContractForAuthenticatedAdmin() throws Exception {
        String token = "banner-update-token";
        mockAuthenticatedAdmin(token, "banner-admin");

        mockMvc.perform(put("/api/content/banners/11")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Banner",
                                  "imageUrl": "https://example.com/banner-update.jpg",
                                  "activityId": 91,
                                  "sort": 3,
                                  "status": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).updateBanner(any(Long.class), any(BannerInfo.class));
    }

    @Test
    void updateBanner_shouldRejectVolunteerRole() throws Exception {
        String token = "banner-update-volunteer-token";
        mockAuthenticatedVolunteer(token, "banner-volunteer");

        mockMvc.perform(put("/api/content/banners/11")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volunteer Banner",
                                  "imageUrl": "https://example.com/banner-update.jpg",
                                  "activityId": 91,
                                  "sort": 3,
                                  "status": 0
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).updateBanner(any(Long.class), any(BannerInfo.class));
    }

    @Test
    void updateBanner_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/banners/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous Banner",
                                  "imageUrl": "https://example.com/banner-update.jpg",
                                  "activityId": 91,
                                  "sort": 3,
                                  "status": 0
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).updateBanner(any(Long.class), any(BannerInfo.class));
    }

    @Test
    void deleteBanner_shouldKeepDeleteContractForAuthenticatedAdmin() throws Exception {
        String token = "banner-delete-token";
        mockAuthenticatedAdmin(token, "banner-admin");

        mockMvc.perform(delete("/api/content/banners/11")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("deleted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).deleteBanner(11L);
    }

    @Test
    void deleteBanner_shouldRejectVolunteerRole() throws Exception {
        String token = "banner-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "banner-volunteer");

        mockMvc.perform(delete("/api/content/banners/11")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).deleteBanner(11L);
    }

    @Test
    void deleteBanner_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/banners/11"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).deleteBanner(11L);
    }

    @Test
    void batchDeleteBanners_shouldKeepBatchDeleteContractForAuthenticatedAdmin() throws Exception {
        String token = "banner-batch-delete-token";
        mockAuthenticatedAdmin(token, "banner-admin");

        mockMvc.perform(post("/api/content/banners/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [11, 12]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch deleted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchDeleteBanners(List.of(11L, 12L));
    }

    @Test
    void batchDeleteBanners_shouldRejectVolunteerRole() throws Exception {
        String token = "banner-batch-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "banner-volunteer");

        mockMvc.perform(post("/api/content/banners/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [11, 12]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchDeleteBanners(List.of(11L, 12L));
    }

    @Test
    void batchDeleteBanners_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/banners/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [11, 12]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchDeleteBanners(List.of(11L, 12L));
    }

    @Test
    void saveForumCategory_shouldKeepCreateContractForAuthenticatedAdmin() throws Exception {
        String token = "forum-category-create-token";
        mockAuthenticatedAdmin(token, "forum-category-admin");

        mockMvc.perform(post("/api/content/forum/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Service Notes",
                                  "sort": 6,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).saveForumCategory(any(ForumCategory.class));
    }

    @Test
    void saveForumCategory_shouldRejectVolunteerRole() throws Exception {
        String token = "forum-category-create-volunteer-token";
        mockAuthenticatedVolunteer(token, "forum-category-volunteer");

        mockMvc.perform(post("/api/content/forum/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Service Notes",
                                  "sort": 6,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).saveForumCategory(any(ForumCategory.class));
    }

    @Test
    void saveForumCategory_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/forum/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Service Notes",
                                  "sort": 6,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).saveForumCategory(any(ForumCategory.class));
    }

    @Test
    void updateForumCategory_shouldKeepUpdateContractForAuthenticatedAdmin() throws Exception {
        String token = "forum-category-update-token";
        mockAuthenticatedAdmin(token, "forum-category-admin");

        mockMvc.perform(put("/api/content/forum/categories/13")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Stories",
                                  "sort": 5,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).updateForumCategory(any(Long.class), any(ForumCategory.class));
    }

    @Test
    void updateForumCategory_shouldRejectVolunteerRole() throws Exception {
        String token = "forum-category-update-volunteer-token";
        mockAuthenticatedVolunteer(token, "forum-category-volunteer");

        mockMvc.perform(put("/api/content/forum/categories/13")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Stories",
                                  "sort": 5,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).updateForumCategory(any(Long.class), any(ForumCategory.class));
    }

    @Test
    void updateForumCategory_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/forum/categories/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Stories",
                                  "sort": 5,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).updateForumCategory(any(Long.class), any(ForumCategory.class));
    }

    @Test
    void deleteForumCategory_shouldKeepDeleteContractForAuthenticatedAdmin() throws Exception {
        String token = "forum-category-delete-token";
        mockAuthenticatedAdmin(token, "forum-category-admin");

        mockMvc.perform(delete("/api/content/forum/categories/13")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("deleted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).deleteForumCategory(13L);
    }

    @Test
    void deleteForumCategory_shouldRejectVolunteerRole() throws Exception {
        String token = "forum-category-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "forum-category-volunteer");

        mockMvc.perform(delete("/api/content/forum/categories/13")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).deleteForumCategory(13L);
    }

    @Test
    void deleteForumCategory_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/forum/categories/13"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).deleteForumCategory(13L);
    }

    @Test
    void batchDeleteForumCategories_shouldKeepBatchDeleteContractForAuthenticatedAdmin() throws Exception {
        String token = "forum-category-batch-delete-token";
        mockAuthenticatedAdmin(token, "forum-category-admin");

        mockMvc.perform(post("/api/content/forum/categories/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [13, 14]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch deleted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchDeleteForumCategories(List.of(13L, 14L));
    }

    @Test
    void batchDeleteForumCategories_shouldRejectVolunteerRole() throws Exception {
        String token = "forum-category-batch-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "forum-category-volunteer");

        mockMvc.perform(post("/api/content/forum/categories/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [13, 14]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchDeleteForumCategories(List.of(13L, 14L));
    }

    @Test
    void batchDeleteForumCategories_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/forum/categories/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [13, 14]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchDeleteForumCategories(List.of(13L, 14L));
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
    void detailForumPost_shouldKeepDetailContract() throws Exception {
        when(contentService.detailForumPost(26L))
                .thenReturn(forumPost(26L, "Neighborhood Support", 6L, 18L, "APPROVED"));

        mockMvc.perform(get("/api/content/forum/posts/26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(26))
                .andExpect(jsonPath("$.data.title").value("Neighborhood Support"))
                .andExpect(jsonPath("$.data.coverImage").value("https://example.com/post-cover.jpg"))
                .andExpect(jsonPath("$.data.summary").value("Community volunteer experience summary"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(contentService).detailForumPost(26L);
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
    void myFavorites_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "favorite-list-admin-token";
        mockAuthenticatedAdmin(token, "favorite-admin");

        mockMvc.perform(get("/api/content/favorites")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).myFavorites();
    }

    @Test
    void myFavorites_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/content/favorites"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).myFavorites();
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
    void pageFavorites_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "favorite-page-admin-token";
        mockAuthenticatedAdmin(token, "favorite-page-admin");

        mockMvc.perform(get("/api/content/favorites/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).pageFavorites(1L, 10L);
    }

    @Test
    void pageFavorites_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/content/favorites/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).pageFavorites(1L, 10L);
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
    void createFavorite_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "favorite-create-admin-token";
        mockAuthenticatedAdmin(token, "favorite-admin");

        mockMvc.perform(post("/api/content/favorites")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": 81,
                                  "note": "Admin should be rejected",
                                  "tag": "Forbidden",
                                  "priority": 5
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).createFavorite(any(FavoriteRequest.class));
    }

    @Test
    void createFavorite_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": 81,
                                  "note": "Anonymous should be rejected",
                                  "tag": "Forbidden",
                                  "priority": 5
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).createFavorite(any(FavoriteRequest.class));
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
    void updateFavorite_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "favorite-update-admin-token";
        mockAuthenticatedAdmin(token, "favorite-update-admin");

        mockMvc.perform(put("/api/content/favorites/18")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "Admin should not update",
                                  "tag": "forbidden",
                                  "priority": 2
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).updateFavorite(any(Long.class), any(FavoriteUpdateRequest.class));
    }

    @Test
    void updateFavorite_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/favorites/18")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "Anonymous should not update",
                                  "tag": "forbidden",
                                  "priority": 2
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).updateFavorite(any(Long.class), any(FavoriteUpdateRequest.class));
    }

    @Test
    void removeFavoriteById_shouldKeepDeleteContractForAuthenticatedVolunteer() throws Exception {
        String token = "favorite-remove-id-token";
        mockAuthenticatedVolunteer(token, "favorite-remove-id-user");

        mockMvc.perform(delete("/api/content/favorites/id/18")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("removed"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).removeFavoriteById(18L);
    }

    @Test
    void removeFavoriteById_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "favorite-remove-id-admin-token";
        mockAuthenticatedAdmin(token, "favorite-remove-id-admin");

        mockMvc.perform(delete("/api/content/favorites/id/18")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).removeFavoriteById(18L);
    }

    @Test
    void removeFavoriteById_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/favorites/id/18"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).removeFavoriteById(18L);
    }

    @Test
    void batchRemoveFavorites_shouldKeepBatchDeleteContractForAuthenticatedVolunteer() throws Exception {
        String token = "favorite-batch-remove-token";
        mockAuthenticatedVolunteer(token, "favorite-batch-remove-user");

        mockMvc.perform(post("/api/content/favorites/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [18, 19, 21]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch removed"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchRemoveFavoriteById(List.of(18L, 19L, 21L));
    }

    @Test
    void batchRemoveFavorites_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "favorite-batch-remove-admin-token";
        mockAuthenticatedAdmin(token, "favorite-batch-remove-admin");

        mockMvc.perform(post("/api/content/favorites/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [18, 19, 21]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchRemoveFavoriteById(List.of(18L, 19L, 21L));
    }

    @Test
    void batchRemoveFavorites_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/favorites/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [18, 19, 21]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchRemoveFavoriteById(List.of(18L, 19L, 21L));
    }

    @Test
    void addFavorite_shouldKeepCreateContractForAuthenticatedVolunteer() throws Exception {
        String token = "favorite-add-token";
        mockAuthenticatedVolunteer(token, "favorite-add-user");

        mockMvc.perform(post("/api/content/favorites/81")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("collected"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).addFavorite(81L);
    }

    @Test
    void addFavorite_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "favorite-add-admin-token";
        mockAuthenticatedAdmin(token, "favorite-add-admin");

        mockMvc.perform(post("/api/content/favorites/81")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).addFavorite(81L);
    }

    @Test
    void addFavorite_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/favorites/81"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).addFavorite(81L);
    }

    @Test
    void removeFavorite_shouldKeepDeleteContractForAuthenticatedVolunteer() throws Exception {
        String token = "favorite-remove-token";
        mockAuthenticatedVolunteer(token, "favorite-remove-user");

        mockMvc.perform(delete("/api/content/favorites/81")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("removed"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).removeFavorite(81L);
    }

    @Test
    void removeFavorite_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "favorite-remove-admin-token";
        mockAuthenticatedAdmin(token, "favorite-remove-admin");

        mockMvc.perform(delete("/api/content/favorites/81")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).removeFavorite(81L);
    }

    @Test
    void removeFavorite_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/favorites/81"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).removeFavorite(81L);
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
                                  "source": "Community Center",
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
    void saveDynamic_shouldRejectVolunteerRole() throws Exception {
        String token = "dynamic-create-volunteer-token";
        mockAuthenticatedVolunteer(token, "dynamic-volunteer");

        mockMvc.perform(post("/api/content/dynamics")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volunteer should not create dynamics",
                                  "source": "Community Center",
                                  "content": "Forbidden",
                                  "imageUrl": "https://example.com/dynamic.png",
                                  "type": "NEWS",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).saveDynamic(any(InfoDynamic.class));
    }

    @Test
    void saveDynamic_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/dynamics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous should not create dynamics",
                                  "source": "Community Center",
                                  "content": "Forbidden",
                                  "imageUrl": "https://example.com/dynamic.png",
                                  "type": "NEWS",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).saveDynamic(any(InfoDynamic.class));
    }

    @Test
    void updateDynamic_shouldKeepUpdateContractForAuthenticatedAdmin() throws Exception {
        String token = "dynamic-update-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(put("/api/content/dynamics/81")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Service Briefing",
                                  "source": "Community Center",
                                  "content": "Updated content",
                                  "imageUrl": "https://example.com/dynamic-update.png",
                                  "type": "NEWS",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).updateDynamic(any(Long.class), any(InfoDynamic.class));
    }

    @Test
    void updateDynamic_shouldRejectVolunteerRole() throws Exception {
        String token = "dynamic-update-volunteer-token";
        mockAuthenticatedVolunteer(token, "dynamic-volunteer");

        mockMvc.perform(put("/api/content/dynamics/81")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volunteer should not update dynamics",
                                  "source": "Community Center",
                                  "content": "Forbidden",
                                  "imageUrl": "https://example.com/dynamic-update.png",
                                  "type": "NEWS",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).updateDynamic(any(Long.class), any(InfoDynamic.class));
    }

    @Test
    void updateDynamic_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/dynamics/81")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous should not update dynamics",
                                  "source": "Community Center",
                                  "content": "Forbidden",
                                  "imageUrl": "https://example.com/dynamic-update.png",
                                  "type": "NEWS",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).updateDynamic(any(Long.class), any(InfoDynamic.class));
    }

    @Test
    void deleteDynamic_shouldKeepArchiveContractForAuthenticatedAdmin() throws Exception {
        String token = "dynamic-delete-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(delete("/api/content/dynamics/81")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("archived"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).deleteDynamic(81L);
    }

    @Test
    void deleteDynamic_shouldRejectVolunteerRole() throws Exception {
        String token = "dynamic-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "dynamic-volunteer");

        mockMvc.perform(delete("/api/content/dynamics/81")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).deleteDynamic(81L);
    }

    @Test
    void deleteDynamic_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/dynamics/81"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).deleteDynamic(81L);
    }

    @Test
    void batchDeleteDynamics_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "dynamic-batch-delete-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(post("/api/content/dynamics/batch-delete")
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

        verify(contentService).batchDeleteDynamics(List.of(81L, 82L));
    }

    @Test
    void batchDeleteDynamics_shouldRejectVolunteerRole() throws Exception {
        String token = "dynamic-batch-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "dynamic-volunteer");

        mockMvc.perform(post("/api/content/dynamics/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [81, 82]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchDeleteDynamics(List.of(81L, 82L));
    }

    @Test
    void batchDeleteDynamics_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/dynamics/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [81, 82]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchDeleteDynamics(List.of(81L, 82L));
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
    void batchArchiveDynamics_shouldRejectVolunteerRole() throws Exception {
        String token = "dynamic-archive-volunteer-token";
        mockAuthenticatedVolunteer(token, "dynamic-volunteer");

        mockMvc.perform(post("/api/content/dynamics/batch-archive")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [81, 82]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchArchiveDynamics(List.of(81L, 82L));
    }

    @Test
    void batchArchiveDynamics_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/dynamics/batch-archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [81, 82]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchArchiveDynamics(List.of(81L, 82L));
    }

    @Test
    void batchRestoreDynamics_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "dynamic-restore-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(post("/api/content/dynamics/batch-restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [81, 82]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch restored"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchRestoreDynamics(List.of(81L, 82L));
    }

    @Test
    void batchRestoreDynamics_shouldRejectVolunteerRole() throws Exception {
        String token = "dynamic-restore-volunteer-token";
        mockAuthenticatedVolunteer(token, "dynamic-volunteer");

        mockMvc.perform(post("/api/content/dynamics/batch-restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [81, 82]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchRestoreDynamics(List.of(81L, 82L));
    }

    @Test
    void batchRestoreDynamics_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/dynamics/batch-restore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [81, 82]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchRestoreDynamics(List.of(81L, 82L));
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
    void saveNotice_shouldRejectVolunteerRole() throws Exception {
        String token = "notice-create-volunteer-token";
        mockAuthenticatedVolunteer(token, "notice-volunteer");

        mockMvc.perform(post("/api/content/notices")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volunteer should not create notices",
                                  "content": "Forbidden",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).saveNotice(any(NoticeInfo.class));
    }

    @Test
    void saveNotice_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous should not create notices",
                                  "content": "Forbidden",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).saveNotice(any(NoticeInfo.class));
    }

    @Test
    void updateNotice_shouldKeepUpdateContractForAuthenticatedAdmin() throws Exception {
        String token = "notice-update-token";
        mockAuthenticatedAdmin(token, "notice-admin");

        mockMvc.perform(put("/api/content/notices/91")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Maintenance Window",
                                  "content": "Updated notice body",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).updateNotice(any(Long.class), any(NoticeInfo.class));
    }

    @Test
    void updateNotice_shouldRejectVolunteerRole() throws Exception {
        String token = "notice-update-volunteer-token";
        mockAuthenticatedVolunteer(token, "notice-volunteer");

        mockMvc.perform(put("/api/content/notices/91")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volunteer should not update notices",
                                  "content": "Forbidden",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).updateNotice(any(Long.class), any(NoticeInfo.class));
    }

    @Test
    void updateNotice_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/notices/91")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous should not update notices",
                                  "content": "Forbidden",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).updateNotice(any(Long.class), any(NoticeInfo.class));
    }

    @Test
    void deleteNotice_shouldKeepArchiveContractForAuthenticatedAdmin() throws Exception {
        String token = "notice-delete-token";
        mockAuthenticatedAdmin(token, "notice-admin");

        mockMvc.perform(delete("/api/content/notices/91")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("archived"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).deleteNotice(91L);
    }

    @Test
    void deleteNotice_shouldRejectVolunteerRole() throws Exception {
        String token = "notice-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "notice-volunteer");

        mockMvc.perform(delete("/api/content/notices/91")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).deleteNotice(91L);
    }

    @Test
    void deleteNotice_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/notices/91"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).deleteNotice(91L);
    }

    @Test
    void batchDeleteNotices_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "notice-batch-delete-token";
        mockAuthenticatedAdmin(token, "notice-admin");

        mockMvc.perform(post("/api/content/notices/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch archived"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchDeleteNotices(List.of(91L, 92L));
    }

    @Test
    void batchDeleteNotices_shouldRejectVolunteerRole() throws Exception {
        String token = "notice-batch-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "notice-volunteer");

        mockMvc.perform(post("/api/content/notices/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchDeleteNotices(List.of(91L, 92L));
    }

    @Test
    void batchDeleteNotices_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/notices/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchDeleteNotices(List.of(91L, 92L));
    }

    @Test
    void batchArchiveNotices_shouldKeepSubmitContractForAuthenticatedAdmin() throws Exception {
        String token = "notice-batch-archive-token";
        mockAuthenticatedAdmin(token, "notice-admin");

        mockMvc.perform(post("/api/content/notices/batch-archive")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch archived"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchArchiveNotices(List.of(91L, 92L));
    }

    @Test
    void batchArchiveNotices_shouldRejectVolunteerRole() throws Exception {
        String token = "notice-batch-archive-volunteer-token";
        mockAuthenticatedVolunteer(token, "notice-volunteer");

        mockMvc.perform(post("/api/content/notices/batch-archive")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchArchiveNotices(List.of(91L, 92L));
    }

    @Test
    void batchArchiveNotices_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/notices/batch-archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchArchiveNotices(List.of(91L, 92L));
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
    void batchRestoreNotices_shouldRejectVolunteerRole() throws Exception {
        String token = "notice-restore-volunteer-token";
        mockAuthenticatedVolunteer(token, "notice-volunteer");

        mockMvc.perform(post("/api/content/notices/batch-restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchRestoreNotices(List.of(91L, 92L));
    }

    @Test
    void batchRestoreNotices_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/notices/batch-restore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [91, 92]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchRestoreNotices(List.of(91L, 92L));
    }

    @Test
    void saveMyPost_shouldKeepSubmitContractForAuthenticatedVolunteer() throws Exception {
        String token = "post-save-token";
        mockAuthenticatedVolunteer(token, "post-author");
        ForumPost saved = forumPost(73L, "Weekend support plan", 5L, 18L, "PENDING");
        when(contentService.saveOrUpdateMyPost(any(ForumPost.class))).thenReturn(saved);

        mockMvc.perform(post("/api/content/forum/posts/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Weekend support plan",
                                  "coverImage": "https://example.com/post-cover.png",
                                  "summary": "Need three volunteers for supplies",
                                  "content": "Need three volunteers for supplies",
                                  "categoryId": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("saved"))
                .andExpect(jsonPath("$.data.id").value(73))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.title").value("Weekend support plan"));

        verify(contentService).saveOrUpdateMyPost(any(ForumPost.class));
    }

    @Test
    void undoMyPost_shouldKeepDeleteContractForAuthenticatedVolunteer() throws Exception {
        String token = "post-undo-token";
        mockAuthenticatedVolunteer(token, "post-author");

        mockMvc.perform(delete("/api/content/forum/posts/my/73/undo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("undone"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).undoMyPostSubmit(73L);
    }

    @Test
    void saveMyPost_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "post-create-admin-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(post("/api/content/forum/posts/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Admin should not create volunteer posts",
                                  "coverImage": "https://example.com/post-cover.png",
                                  "summary": "Forbidden role path",
                                  "content": "Forbidden role path",
                                  "categoryId": 5
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(contentService, never()).saveOrUpdateMyPost(any(ForumPost.class));
    }

    @Test
    void saveMyPost_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/forum/posts/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous should not create posts",
                                  "coverImage": "https://example.com/post-cover.png",
                                  "summary": "Forbidden anonymous path",
                                  "content": "Forbidden anonymous path",
                                  "categoryId": 5
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).saveOrUpdateMyPost(any(ForumPost.class));
    }

    @Test
    void undoMyPost_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "post-undo-admin-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(delete("/api/content/forum/posts/my/73/undo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(contentService, never()).undoMyPostSubmit(73L);
    }

    @Test
    void undoMyPost_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/forum/posts/my/73/undo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).undoMyPostSubmit(73L);
    }

    @Test
    void updateForumPost_shouldKeepUpdateContractForAuthenticatedAdmin() throws Exception {
        String token = "post-update-admin-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(put("/api/content/forum/posts/73")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated support plan",
                                  "coverImage": "https://example.com/post-cover.png",
                                  "summary": "Updated summary",
                                  "content": "Updated content",
                                  "categoryId": 5,
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).updateForumPost(any(Long.class), any(ForumPost.class));
    }

    @Test
    void updateForumPost_shouldRejectVolunteerRole() throws Exception {
        String token = "post-update-volunteer-token";
        mockAuthenticatedVolunteer(token, "post-volunteer");

        mockMvc.perform(put("/api/content/forum/posts/73")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volunteer should not update",
                                  "coverImage": "https://example.com/post-cover.png",
                                  "summary": "Forbidden",
                                  "content": "Forbidden",
                                  "categoryId": 5,
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).updateForumPost(any(Long.class), any(ForumPost.class));
    }

    @Test
    void updateForumPost_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/forum/posts/73")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Anonymous should not update",
                                  "coverImage": "https://example.com/post-cover.png",
                                  "summary": "Forbidden",
                                  "content": "Forbidden",
                                  "categoryId": 5,
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).updateForumPost(any(Long.class), any(ForumPost.class));
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
    void auditPost_shouldRejectVolunteerRole() throws Exception {
        String token = "post-audit-volunteer-token";
        mockAuthenticatedVolunteer(token, "post-volunteer");

        mockMvc.perform(put("/api/content/forum/posts/73/audit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("status", "REJECTED")
                        .param("reason", "forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).auditPost(73L, "REJECTED", "forbidden");
    }

    @Test
    void auditPost_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/forum/posts/73/audit")
                        .param("status", "REJECTED")
                        .param("reason", "forbidden"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).auditPost(73L, "REJECTED", "forbidden");
    }

    @Test
    void deletePost_shouldKeepArchiveContractForAuthenticatedAdmin() throws Exception {
        String token = "post-delete-admin-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(delete("/api/content/forum/posts/73")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("archived"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).deleteForumPost(73L);
    }

    @Test
    void deletePost_shouldRejectVolunteerRole() throws Exception {
        String token = "post-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "post-volunteer");

        mockMvc.perform(delete("/api/content/forum/posts/73")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).deleteForumPost(73L);
    }

    @Test
    void deletePost_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/forum/posts/73"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).deleteForumPost(73L);
    }

    @Test
    void batchDeletePosts_shouldKeepBatchArchiveContractForAuthenticatedAdmin() throws Exception {
        String token = "post-batch-delete-admin-token";
        mockAuthenticatedAdmin(token, "content-admin");

        mockMvc.perform(post("/api/content/forum/posts/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [73, 74]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch archived"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchDeleteForumPosts(List.of(73L, 74L));
    }

    @Test
    void batchDeletePosts_shouldRejectVolunteerRole() throws Exception {
        String token = "post-batch-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "post-volunteer");

        mockMvc.perform(post("/api/content/forum/posts/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [73, 74]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchDeleteForumPosts(List.of(73L, 74L));
    }

    @Test
    void batchDeletePosts_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/forum/posts/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [73, 74]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchDeleteForumPosts(List.of(73L, 74L));
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

    @Test
    void addComment_shouldKeepCreateContractForAuthenticatedAdmin() throws Exception {
        String token = "comment-create-admin-token";
        mockAuthenticatedAdmin(token, "comment-admin");

        mockMvc.perform(post("/api/content/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "DYNAMIC",
                                  "targetId": 52,
                                  "content": "Admin moderation note"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        ArgumentCaptor<CommentInfo> commentCaptor = ArgumentCaptor.forClass(CommentInfo.class);
        verify(contentService).addComment(commentCaptor.capture());
        Assertions.assertEquals("DYNAMIC", commentCaptor.getValue().getTargetType());
        Assertions.assertEquals(52L, commentCaptor.getValue().getTargetId());
        Assertions.assertEquals("Admin moderation note", commentCaptor.getValue().getContent());
        Assertions.assertNull(commentCaptor.getValue().getTestDataTag());
    }

    @Test
    void addComment_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "POST",
                                  "targetId": 51,
                                  "content": "Anonymous should be blocked"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).addComment(any(CommentInfo.class));
    }

    @Test
    void pageComments_shouldKeepPublicPageContractForAnonymousUser() throws Exception {
        when(contentService.pageComments(2L, 5L, false, "POST", 51L, false))
                .thenReturn(pageResult(2, 5, comment(17L, "POST", 51L, "Portal visitor feedback", null)));

        mockMvc.perform(get("/api/content/comments/page")
                        .param("current", "2")
                        .param("size", "5")
                        .param("targetType", "POST")
                        .param("targetId", "51")
                        .param("includeTestData", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.records[0].id").value(17))
                .andExpect(jsonPath("$.data.records[0].targetType").value("POST"))
                .andExpect(jsonPath("$.data.records[0].targetId").value(51))
                .andExpect(jsonPath("$.data.records[0].content").value("Portal visitor feedback"));

        verify(contentService).pageComments(2L, 5L, false, "POST", 51L, false);
    }

    @Test
    void pageComments_shouldRejectAnonymousOnlyMineQuery() throws Exception {
        mockMvc.perform(get("/api/content/comments/page")
                        .param("onlyMine", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).pageComments(1L, 10L, true, null, null, false);
    }

    @Test
    void deleteComment_shouldKeepDeleteContractForAuthenticatedAdmin() throws Exception {
        String token = "comment-delete-admin-token";
        mockAuthenticatedAdmin(token, "comment-admin");

        mockMvc.perform(delete("/api/content/comments/17")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("deleted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).deleteComment(17L);
    }

    @Test
    void deleteComment_shouldRejectVolunteerRole() throws Exception {
        String token = "comment-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "comment-volunteer");

        mockMvc.perform(delete("/api/content/comments/17")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).deleteComment(17L);
    }

    @Test
    void deleteComment_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/comments/17"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).deleteComment(17L);
    }

    @Test
    void batchDeleteComments_shouldKeepBatchDeleteContractForAuthenticatedAdmin() throws Exception {
        String token = "comment-batch-delete-admin-token";
        mockAuthenticatedAdmin(token, "comment-admin");

        mockMvc.perform(post("/api/content/comments/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [17, 18]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch deleted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchDeleteComments(List.of(17L, 18L));
    }

    @Test
    void batchDeleteComments_shouldRejectVolunteerRole() throws Exception {
        String token = "comment-batch-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "comment-volunteer");

        mockMvc.perform(post("/api/content/comments/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [17, 18]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchDeleteComments(List.of(17L, 18L));
    }

    @Test
    void batchDeleteComments_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/comments/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [17, 18]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchDeleteComments(List.of(17L, 18L));
    }

    @Test
    void pageMallProducts_shouldKeepPageContract() throws Exception {
        when(contentService.pageMallProducts(2L, 8L, "badge", 1, true))
                .thenReturn(pageResult(2, 8, mallProduct(21L, "Volunteer Badge", 1, 5)));

        mockMvc.perform(get("/api/content/mall-products/page")
                        .param("current", "2")
                        .param("size", "8")
                        .param("keyword", "badge")
                        .param("status", "1")
                        .param("onlyEnabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(8))
                .andExpect(jsonPath("$.data.records[0].id").value(21))
                .andExpect(jsonPath("$.data.records[0].name").value("Volunteer Badge"))
                .andExpect(jsonPath("$.data.records[0].pointsCost").value(120))
                .andExpect(jsonPath("$.data.records[0].stock").value(5))
                .andExpect(jsonPath("$.data.records[0].status").value(1));

        verify(contentService).pageMallProducts(2L, 8L, "badge", 1, true);
    }

    @Test
    void pageAdminMallProducts_shouldKeepPageContractForAuthenticatedAdmin() throws Exception {
        String token = "mall-admin-token";
        mockAuthenticatedAdmin(token, "mall-admin");
        when(contentService.pageMallProducts(1L, 10L, "kit", 0, false))
                .thenReturn(pageResult(1, 10, mallProduct(22L, "Emergency Kit", 0, 0)));

        mockMvc.perform(get("/api/content/admin/mall-products/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("keyword", "kit")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(22))
                .andExpect(jsonPath("$.data.records[0].name").value("Emergency Kit"))
                .andExpect(jsonPath("$.data.records[0].status").value(0));

        verify(contentService).pageMallProducts(1L, 10L, "kit", 0, false);
    }

    @Test
    void pageAdminMallProducts_shouldRejectVolunteerRole() throws Exception {
        String token = "mall-volunteer-token";
        mockAuthenticatedVolunteer(token, "mall-volunteer");

        mockMvc.perform(get("/api/content/admin/mall-products/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(contentService, never()).pageMallProducts(1L, 10L, null, null, false);
    }

    @Test
    void pageAdminMallProducts_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/content/admin/mall-products/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).pageMallProducts(1L, 10L, null, null, false);
    }

    @Test
    void saveMallProduct_shouldKeepCreateContractForAuthenticatedAdmin() throws Exception {
        String token = "mall-create-token";
        mockAuthenticatedAdmin(token, "mall-admin");

        mockMvc.perform(post("/api/content/mall-products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Gloves",
                                  "imageUrl": "https://example.com/mall-product.jpg",
                                  "summary": "Protection supplies",
                                  "pointsCost": 180,
                                  "stock": 20,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).saveMallProduct(any(com.community.modules.content.dto.MallProductRequest.class));
    }

    @Test
    void saveMallProduct_shouldRejectVolunteerRole() throws Exception {
        String token = "mall-create-volunteer-token";
        mockAuthenticatedVolunteer(token, "mall-volunteer");

        mockMvc.perform(post("/api/content/mall-products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Gloves",
                                  "imageUrl": "https://example.com/mall-product.jpg",
                                  "summary": "Protection supplies",
                                  "pointsCost": 180,
                                  "stock": 20,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).saveMallProduct(any(com.community.modules.content.dto.MallProductRequest.class));
    }

    @Test
    void saveMallProduct_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/mall-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Gloves",
                                  "imageUrl": "https://example.com/mall-product.jpg",
                                  "summary": "Protection supplies",
                                  "pointsCost": 180,
                                  "stock": 20,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).saveMallProduct(any(com.community.modules.content.dto.MallProductRequest.class));
    }

    @Test
    void updateMallProduct_shouldKeepUpdateContractForAuthenticatedAdmin() throws Exception {
        String token = "mall-update-token";
        mockAuthenticatedAdmin(token, "mall-admin");

        mockMvc.perform(put("/api/content/mall-products/21")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Badge Plus",
                                  "imageUrl": "https://example.com/mall-product.jpg",
                                  "summary": "Upgraded badge",
                                  "pointsCost": 220,
                                  "stock": 8,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).updateMallProduct(any(Long.class),
                any(com.community.modules.content.dto.MallProductRequest.class));
    }

    @Test
    void updateMallProduct_shouldRejectVolunteerRole() throws Exception {
        String token = "mall-update-volunteer-token";
        mockAuthenticatedVolunteer(token, "mall-volunteer");

        mockMvc.perform(put("/api/content/mall-products/21")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Badge Plus",
                                  "imageUrl": "https://example.com/mall-product.jpg",
                                  "summary": "Upgraded badge",
                                  "pointsCost": 220,
                                  "stock": 8,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).updateMallProduct(any(Long.class),
                any(com.community.modules.content.dto.MallProductRequest.class));
    }

    @Test
    void updateMallProduct_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/mall-products/21")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Volunteer Badge Plus",
                                  "imageUrl": "https://example.com/mall-product.jpg",
                                  "summary": "Upgraded badge",
                                  "pointsCost": 220,
                                  "stock": 8,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).updateMallProduct(any(Long.class),
                any(com.community.modules.content.dto.MallProductRequest.class));
    }

    @Test
    void disableMallProduct_shouldKeepDisableContractForAuthenticatedAdmin() throws Exception {
        String token = "mall-disable-token";
        mockAuthenticatedAdmin(token, "mall-admin");

        mockMvc.perform(delete("/api/content/mall-products/21")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("disabled"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).disableMallProduct(21L);
    }

    @Test
    void disableMallProduct_shouldRejectVolunteerRole() throws Exception {
        String token = "mall-disable-volunteer-token";
        mockAuthenticatedVolunteer(token, "mall-volunteer");

        mockMvc.perform(delete("/api/content/mall-products/21")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).disableMallProduct(21L);
    }

    @Test
    void disableMallProduct_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/content/mall-products/21"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).disableMallProduct(21L);
    }

    @Test
    void enableMallProduct_shouldKeepEnableContractForAuthenticatedAdmin() throws Exception {
        String token = "mall-enable-token";
        mockAuthenticatedAdmin(token, "mall-admin");

        mockMvc.perform(put("/api/content/mall-products/21/enable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("enabled"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).enableMallProduct(21L);
    }

    @Test
    void enableMallProduct_shouldRejectVolunteerRole() throws Exception {
        String token = "mall-enable-volunteer-token";
        mockAuthenticatedVolunteer(token, "mall-volunteer");

        mockMvc.perform(put("/api/content/mall-products/21/enable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).enableMallProduct(21L);
    }

    @Test
    void enableMallProduct_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/mall-products/21/enable"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).enableMallProduct(21L);
    }

    @Test
    void batchDisableMallProducts_shouldKeepBatchDisableContractForAuthenticatedAdmin() throws Exception {
        String token = "mall-batch-disable-token";
        mockAuthenticatedAdmin(token, "mall-admin");

        mockMvc.perform(post("/api/content/mall-products/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [21, 22]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch disabled"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchDisableMallProducts(List.of(21L, 22L));
    }

    @Test
    void batchDisableMallProducts_shouldRejectVolunteerRole() throws Exception {
        String token = "mall-batch-disable-volunteer-token";
        mockAuthenticatedVolunteer(token, "mall-volunteer");

        mockMvc.perform(post("/api/content/mall-products/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [21, 22]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchDisableMallProducts(List.of(21L, 22L));
    }

    @Test
    void batchDisableMallProducts_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/mall-products/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [21, 22]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchDisableMallProducts(List.of(21L, 22L));
    }

    @Test
    void batchEnableMallProducts_shouldKeepBatchEnableContractForAuthenticatedAdmin() throws Exception {
        String token = "mall-batch-enable-token";
        mockAuthenticatedAdmin(token, "mall-admin");

        mockMvc.perform(post("/api/content/mall-products/batch-enable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [21, 22]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch enabled"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).batchEnableMallProducts(List.of(21L, 22L));
    }

    @Test
    void batchEnableMallProducts_shouldRejectVolunteerRole() throws Exception {
        String token = "mall-batch-enable-volunteer-token";
        mockAuthenticatedVolunteer(token, "mall-volunteer");

        mockMvc.perform(post("/api/content/mall-products/batch-enable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [21, 22]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).batchEnableMallProducts(List.of(21L, 22L));
    }

    @Test
    void batchEnableMallProducts_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/content/mall-products/batch-enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [21, 22]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).batchEnableMallProducts(List.of(21L, 22L));
    }

    @Test
    void pageAdminExchangeOrders_shouldRejectVolunteerRole() throws Exception {
        String token = "exchange-admin-forbidden-token";
        mockAuthenticatedVolunteer(token, "exchange-volunteer");

        mockMvc.perform(get("/api/content/admin/orders/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));
    }

    @Test
    void createExchangeOrder_shouldKeepCreateContractForAuthenticatedVolunteer() throws Exception {
        String token = "exchange-create-token";
        mockAuthenticatedVolunteer(token, "exchange-user");
        when(contentService.createExchangeOrder(any(com.community.modules.content.dto.ExchangeOrderCreateRequest.class)))
                .thenReturn(exchangeOrder(31L, "ORD202603140031", "CREATED"));

        mockMvc.perform(post("/api/content/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 21,
                                  "quantity": 2,
                                  "receiverName": "Volunteer User",
                                  "receiverPhone": "13800138000",
                                  "receiverAddress": "Community Service Center",
                                  "requestKey": "req-exchange-21"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data.id").value(31))
                .andExpect(jsonPath("$.data.orderNo").value("ORD202603140031"))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.productName").value("Volunteer Badge"))
                .andExpect(jsonPath("$.data.totalPoints").value(240));

        verify(contentService).createExchangeOrder(any());
    }

    @Test
    void createExchangeOrder_shouldRejectAdminRole() throws Exception {
        String token = "exchange-create-admin-token";
        mockAuthenticatedAdmin(token, "exchange-admin");

        mockMvc.perform(post("/api/content/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 21,
                                  "quantity": 2,
                                  "receiverName": "Admin User",
                                  "receiverPhone": "13800138000",
                                  "receiverAddress": "Community Service Center",
                                  "requestKey": "req-admin-exchange-21"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));
    }

    @Test
    void pageMyExchangeOrders_shouldKeepPageContractForAuthenticatedVolunteer() throws Exception {
        String token = "exchange-page-token";
        mockAuthenticatedVolunteer(token, "exchange-user");
        when(contentService.pageMyExchangeOrders(3L, 5L, "SHIPPED"))
                .thenReturn(pageResult(3, 5, exchangeOrder(32L, "ORD202603140032", "SHIPPED")));

        mockMvc.perform(get("/api/content/orders/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "3")
                        .param("size", "5")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(3))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.records[0].id").value(32))
                .andExpect(jsonPath("$.data.records[0].status").value("SHIPPED"))
                .andExpect(jsonPath("$.data.records[0].receiverPhone").value("13800138000"));

        verify(contentService).pageMyExchangeOrders(3L, 5L, "SHIPPED");
    }

    @Test
    void pageMyExchangeOrders_shouldRejectAuthenticatedAdmin() throws Exception {
        String token = "exchange-page-admin-token";
        mockAuthenticatedAdmin(token, "exchange-admin");

        mockMvc.perform(get("/api/content/orders/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(contentService, never()).pageMyExchangeOrders(1L, 10L, null);
    }

    @Test
    void pageMyExchangeOrders_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/content/orders/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).pageMyExchangeOrders(1L, 10L, null);
    }

    @Test
    void pageAdminExchangeOrders_shouldKeepPageContractForAuthenticatedAdmin() throws Exception {
        String token = "exchange-admin-token";
        mockAuthenticatedAdmin(token, "exchange-admin");
        ExchangeOrder maskedOrder = exchangeOrder(33L, "ORD202603140033", "CREATED");
        maskedOrder.setReceiverPhone("138****8000");
        maskedOrder.setReceiverAddress("Commun****ng A");
        when(contentService.pageAdminExchangeOrders(1L, 10L, "ORD20260314", "badge", "volunteer", "CREATED"))
                .thenReturn(pageResult(1, 10, maskedOrder));

        mockMvc.perform(get("/api/content/admin/orders/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("orderNo", "ORD20260314")
                        .param("productKeyword", "badge")
                        .param("userKeyword", "volunteer")
                        .param("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(33))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("ORD202603140033"))
                .andExpect(jsonPath("$.data.records[0].status").value("CREATED"))
                .andExpect(jsonPath("$.data.records[0].receiverPhone").value("138****8000"))
                .andExpect(jsonPath("$.data.records[0].receiverAddress").value("Commun****ng A"));

        verify(contentService).pageAdminExchangeOrders(1L, 10L, "ORD20260314", "badge", "volunteer", "CREATED");
    }

    @Test
    void pageAdminExchangeOrders_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/content/admin/orders/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).pageAdminExchangeOrders(1L, 10L, null, null, null, null);
    }

    @Test
    void detailAdminExchangeOrder_shouldKeepDetailContractForAuthenticatedAdmin() throws Exception {
        String token = "exchange-detail-admin-token";
        mockAuthenticatedAdmin(token, "exchange-admin");
        when(contentService.detailAdminExchangeOrder(34L))
                .thenReturn(exchangeOrder(34L, "ORD202603140034", "RECEIVED"));

        mockMvc.perform(get("/api/content/admin/orders/34")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(34))
                .andExpect(jsonPath("$.data.orderNo").value("ORD202603140034"))
                .andExpect(jsonPath("$.data.receiverName").value("Volunteer User"))
                .andExpect(jsonPath("$.data.receiverPhone").value("13800138000"))
                .andExpect(jsonPath("$.data.status").value("RECEIVED"));

        verify(contentService).detailAdminExchangeOrder(34L);
    }

    @Test
    void detailAdminExchangeOrder_shouldRejectVolunteerRole() throws Exception {
        String token = "exchange-detail-volunteer-token";
        mockAuthenticatedVolunteer(token, "exchange-volunteer");

        mockMvc.perform(get("/api/content/admin/orders/34")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(contentService, never()).detailAdminExchangeOrder(34L);
    }

    @Test
    void detailAdminExchangeOrder_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/content/admin/orders/34"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).detailAdminExchangeOrder(34L);
    }

    @Test
    void updateExchangeOrderStatus_shouldKeepUpdateContractForAuthenticatedAdmin() throws Exception {
        String token = "exchange-status-admin-token";
        mockAuthenticatedAdmin(token, "exchange-admin");

        mockMvc.perform(put("/api/content/orders/35/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CANCELLED",
                                  "reason": "inventory review"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(contentService).updateExchangeOrderStatus(any(Long.class),
                any(com.community.modules.content.dto.ExchangeOrderStatusRequest.class));
    }

    @Test
    void updateExchangeOrderStatus_shouldRejectVolunteerRole() throws Exception {
        String token = "exchange-status-volunteer-token";
        mockAuthenticatedVolunteer(token, "exchange-volunteer");

        mockMvc.perform(put("/api/content/orders/35/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CANCELLED",
                                  "reason": "volunteer should not update"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(contentService, never()).updateExchangeOrderStatus(any(Long.class),
                any(com.community.modules.content.dto.ExchangeOrderStatusRequest.class));
    }

    @Test
    void updateExchangeOrderStatus_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/content/orders/35/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CANCELLED",
                                  "reason": "anonymous should not update"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(contentService, never()).updateExchangeOrderStatus(any(Long.class),
                any(com.community.modules.content.dto.ExchangeOrderStatusRequest.class));
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
        post.setCoverImage("https://example.com/post-cover.jpg");
        post.setSummary("Community volunteer experience summary");
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
        dynamic.setSource("Community Center");
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

    private MallProduct mallProduct(Long id, String name, Integer status, Integer stock) {
        MallProduct product = new MallProduct();
        product.setId(id);
        product.setName(name);
        product.setImageUrl("https://example.com/mall-product.jpg");
        product.setSummary("Redeemable volunteer supply");
        product.setPointsCost(120);
        product.setStock(stock);
        product.setStatus(status);
        return product;
    }

    private ExchangeOrder exchangeOrder(Long id, String orderNo, String status) {
        ExchangeOrder order = new ExchangeOrder();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setUserId(9L);
        order.setProductId(21L);
        order.setUserName("volunteer_user");
        order.setRealName("Volunteer User");
        order.setProductName("Volunteer Badge");
        order.setProductImage("https://example.com/badge.jpg");
        order.setProductSummary("Redeemable volunteer badge");
        order.setQuantity(2);
        order.setPointsPerItem(120);
        order.setTotalPoints(240);
        order.setReceiverName("Volunteer User");
        order.setReceiverPhone("13800138000");
        order.setReceiverAddress("Community Service Center Building A");
        order.setStatus(status);
        order.setStatusReason("approved");
        order.setCreateTime(LocalDateTime.of(2026, 3, 14, 11, 0));
        return order;
    }

    private CommentInfo comment(Long id, String targetType, Long targetId, String content, String testDataTag) {
        CommentInfo comment = new CommentInfo();
        comment.setId(id);
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        comment.setUserId(9L);
        comment.setContent(content);
        comment.setTestDataTag(testDataTag);
        comment.setStatus(1);
        return comment;
    }
}
