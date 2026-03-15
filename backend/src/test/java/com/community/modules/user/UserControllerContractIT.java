package com.community.modules.user;

import com.community.common.config.JwtAuthenticationFilter;
import com.community.common.config.SecurityConfig;
import com.community.common.util.JwtTokenUtil;
import com.community.common.web.PageResult;
import com.community.modules.auth.service.DbUserDetailsService;
import com.community.modules.user.controller.UserController;
import com.community.modules.user.entity.User;
import com.community.modules.user.entity.VolunteerCertification;
import com.community.modules.user.service.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.userdetails.User.withUsername;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerContractIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private DbUserDetailsService dbUserDetailsService;

    @Test
    void page_shouldKeepMaskedListContractForAdmin() throws Exception {
        String token = "user-page-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");
        when(userService.pageUsers(2L, 5L, "VOLUNTEER", "alice", 1, 1, "PENDING"))
                .thenReturn(new PageResult<>(1, 2, 5, List.of(user(31L, "alice.volunteer", "138****5678", "a***@example.com", "VOLUNTEER"))));

        mockMvc.perform(get("/api/users/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "2")
                        .param("size", "5")
                        .param("role", "VOLUNTEER")
                        .param("keyword", "alice")
                        .param("status", "1")
                        .param("certified", "1")
                        .param("certificationStatus", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.records[0].id").value(31))
                .andExpect(jsonPath("$.data.records[0].username").value("alice.volunteer"))
                .andExpect(jsonPath("$.data.records[0].phone").value("138****5678"))
                .andExpect(jsonPath("$.data.records[0].email").value("a***@example.com"))
                .andExpect(jsonPath("$.data.records[0].certificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.records[0].role").value("VOLUNTEER"));

        verify(userService).pageUsers(2L, 5L, "VOLUNTEER", "alice", 1, 1, "PENDING");
    }

    @Test
    void page_shouldRejectVolunteerRole() throws Exception {
        String token = "user-page-volunteer-token";
        mockAuthenticatedVolunteer(token, "user-volunteer");

        mockMvc.perform(get("/api/users/page")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(userService, never()).pageUsers(1L, 10L, null, null, null, null, null);
    }

    @Test
    void page_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/users/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).pageUsers(1L, 10L, null, null, null, null, null);
    }

    @Test
    void detail_shouldKeepFullDetailContractForAdmin() throws Exception {
        String token = "user-detail-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");
        when(userService.getUserDetail(41L)).thenReturn(user(41L, "ops-admin", "13900001111", "ops-admin@example.com", "ADMIN"));

        mockMvc.perform(get("/api/users/41")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(41))
                .andExpect(jsonPath("$.data.username").value("ops-admin"))
                .andExpect(jsonPath("$.data.phone").value("13900001111"))
                .andExpect(jsonPath("$.data.email").value("ops-admin@example.com"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        verify(userService).getUserDetail(41L);
    }

    @Test
    void detail_shouldRejectVolunteerRole() throws Exception {
        String token = "user-detail-volunteer-token";
        mockAuthenticatedVolunteer(token, "user-volunteer");

        mockMvc.perform(get("/api/users/41")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(userService, never()).getUserDetail(41L);
    }

    @Test
    void detail_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/users/41"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).getUserDetail(41L);
    }

    @Test
    void createUser_shouldKeepSubmitContractForAdmin() throws Exception {
        String token = "user-create-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");

        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "new-admin",
                                  "password": "admin123",
                                  "realName": "New Admin",
                                  "phone": "13900002222",
                                  "email": "new-admin@example.com",
                                  "role": "ADMIN",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(userService).createUser(any());
    }

    @Test
    void createUser_shouldRejectVolunteerRole() throws Exception {
        String token = "user-create-volunteer-token";
        mockAuthenticatedVolunteer(token, "user-volunteer");

        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "new-volunteer",
                                  "password": "volunteer123",
                                  "realName": "Volunteer User",
                                  "phone": "13900003333",
                                  "email": "new-volunteer@example.com",
                                  "role": "VOLUNTEER",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anonymous",
                                  "password": "anonymous123",
                                  "realName": "Anonymous User",
                                  "phone": "13900004444",
                                  "email": "anonymous@example.com",
                                  "role": "VOLUNTEER",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).createUser(any());
    }

    @Test
    void updateUser_shouldKeepSubmitContractForAdmin() throws Exception {
        String token = "user-update-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");

        mockMvc.perform(put("/api/users/53")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "updated-admin",
                                  "realName": "Updated Admin",
                                  "phone": "13900005555",
                                  "email": "updated-admin@example.com",
                                  "role": "ADMIN",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(userService).adminUpdateUser(eq(53L), any());
    }

    @Test
    void updateUser_shouldRejectVolunteerRole() throws Exception {
        String token = "user-update-volunteer-token";
        mockAuthenticatedVolunteer(token, "user-volunteer");

        mockMvc.perform(put("/api/users/53")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "volunteer-update",
                                  "realName": "Volunteer Update",
                                  "phone": "13900005555",
                                  "email": "volunteer-update@example.com",
                                  "role": "VOLUNTEER",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(userService, never()).adminUpdateUser(eq(53L), any());
    }

    @Test
    void updateUser_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/users/53")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anonymous-update",
                                  "realName": "Anonymous Update",
                                  "phone": "13900005555",
                                  "email": "anonymous-update@example.com",
                                  "role": "VOLUNTEER",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).adminUpdateUser(eq(53L), any());
    }

    @Test
    void disableAndEnable_shouldKeepSubmitContractForAdmin() throws Exception {
        String token = "user-toggle-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");

        mockMvc.perform(put("/api/users/52/disable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("disabled"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        mockMvc.perform(put("/api/users/52/enable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("enabled"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(userService).disableUser(52L);
        verify(userService).enableUser(52L);
    }

    @Test
    void disableAndEnable_shouldRejectVolunteerRole() throws Exception {
        String token = "user-toggle-volunteer-token";
        mockAuthenticatedVolunteer(token, "user-volunteer");

        mockMvc.perform(put("/api/users/52/disable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        mockMvc.perform(put("/api/users/52/enable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(userService, never()).disableUser(52L);
        verify(userService, never()).enableUser(52L);
    }

    @Test
    void disableAndEnable_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/users/52/disable"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        mockMvc.perform(put("/api/users/52/enable"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).disableUser(52L);
        verify(userService, never()).enableUser(52L);
    }

    @Test
    void batchDisableAndEnable_shouldKeepSubmitContractForAdmin() throws Exception {
        String token = "user-batch-toggle-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");

        mockMvc.perform(post("/api/users/batch-disable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [61, 62]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch disabled"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        mockMvc.perform(post("/api/users/batch-enable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [61, 62]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch enabled"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(userService).batchDisableUsers(List.of(61L, 62L));
        verify(userService).batchEnableUsers(List.of(61L, 62L));
    }

    @Test
    void batchDisableAndEnable_shouldRejectVolunteerRole() throws Exception {
        String token = "user-batch-toggle-volunteer-token";
        mockAuthenticatedVolunteer(token, "user-volunteer");

        mockMvc.perform(post("/api/users/batch-disable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [61, 62]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        mockMvc.perform(post("/api/users/batch-enable")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [61, 62]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(userService, never()).batchDisableUsers(List.of(61L, 62L));
        verify(userService, never()).batchEnableUsers(List.of(61L, 62L));
    }

    @Test
    void batchDisableAndEnable_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/users/batch-disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [61, 62]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        mockMvc.perform(post("/api/users/batch-enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [61, 62]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).batchDisableUsers(List.of(61L, 62L));
        verify(userService, never()).batchEnableUsers(List.of(61L, 62L));
    }

    @Test
    void deleteAndBatchDelete_shouldKeepSubmitContractForAdmin() throws Exception {
        String token = "user-delete-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");

        mockMvc.perform(delete("/api/users/71")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("deleted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        mockMvc.perform(post("/api/users/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [71, 72]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("batch deleted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(userService).deleteVolunteer(71L);
        verify(userService).batchDeleteVolunteers(List.of(71L, 72L));
    }

    @Test
    void deleteAndBatchDelete_shouldRejectVolunteerRole() throws Exception {
        String token = "user-delete-volunteer-token";
        mockAuthenticatedVolunteer(token, "user-volunteer");

        mockMvc.perform(delete("/api/users/71")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        mockMvc.perform(post("/api/users/batch-delete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [71, 72]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(userService, never()).deleteVolunteer(71L);
        verify(userService, never()).batchDeleteVolunteers(List.of(71L, 72L));
    }

    @Test
    void deleteAndBatchDelete_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(delete("/api/users/71"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        mockMvc.perform(post("/api/users/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [71, 72]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).deleteVolunteer(71L);
        verify(userService, never()).batchDeleteVolunteers(List.of(71L, 72L));
    }

    @Test
    void me_shouldKeepDetailContractForAuthenticatedUser() throws Exception {
        String token = "user-me-volunteer-token";
        mockAuthenticatedVolunteer(token, "volunteer-user");
        when(userService.getCurrentUser()).thenReturn(user(81L, "volunteer-user", "13900006666", "volunteer-user@example.com", "VOLUNTEER"));

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(81))
                .andExpect(jsonPath("$.data.username").value("volunteer-user"))
                .andExpect(jsonPath("$.data.role").value("VOLUNTEER"));

        verify(userService).getCurrentUser();
    }

    @Test
    void me_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).getCurrentUser();
    }

    @Test
    void myCertification_shouldKeepDetailContractForVolunteer() throws Exception {
        String token = "my-certification-token";
        mockAuthenticatedVolunteer(token, "volunteer-user");
        when(userService.getCurrentUserCertification()).thenReturn(certification(61L, 31L, "PENDING"));

        mockMvc.perform(get("/api/users/me/certification")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(61))
                .andExpect(jsonPath("$.data.userId").value(31))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.idCardNo").value("330101199001011234"));

        verify(userService).getCurrentUserCertification();
    }

    @Test
    void myCertification_shouldRejectAdminRole() throws Exception {
        String token = "my-certification-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");

        mockMvc.perform(get("/api/users/me/certification")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(userService, never()).getCurrentUserCertification();
    }

    @Test
    void myCertification_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/users/me/certification"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).getCurrentUserCertification();
    }

    @Test
    void submitMyCertification_shouldKeepSubmitContractForVolunteer() throws Exception {
        String token = "submit-certification-token";
        mockAuthenticatedVolunteer(token, "volunteer-user");

        mockMvc.perform(post("/api/users/me/certification")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "realName": "Volunteer User",
                                  "idCardNo": "330101199001011234",
                                  "idCardFrontUrl": "https://example.com/id-front.jpg",
                                  "idCardBackUrl": "https://example.com/id-back.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("submitted"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(userService).submitCurrentUserCertification(any());
    }

    @Test
    void submitMyCertification_shouldRejectAdminRole() throws Exception {
        String token = "submit-certification-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");

        mockMvc.perform(post("/api/users/me/certification")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "realName": "Admin User",
                                  "idCardNo": "330101199001011234",
                                  "idCardFrontUrl": "https://example.com/id-front.jpg",
                                  "idCardBackUrl": "https://example.com/id-back.jpg"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("无权限访问该资源"));

        verify(userService, never()).submitCurrentUserCertification(any());
    }

    @Test
    void submitMyCertification_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(post("/api/users/me/certification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "realName": "Anonymous User",
                                  "idCardNo": "330101199001011234",
                                  "idCardFrontUrl": "https://example.com/id-front.jpg",
                                  "idCardBackUrl": "https://example.com/id-back.jpg"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).submitCurrentUserCertification(any());
    }

    @Test
    void certificationDetailAndAudit_shouldKeepContractForAdmin() throws Exception {
        String token = "audit-certification-admin-token";
        mockAuthenticatedAdmin(token, "user-admin");
        when(userService.getUserCertificationDetail(31L)).thenReturn(certification(62L, 31L, "REJECTED"));

        mockMvc.perform(get("/api/users/31/certification")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(62))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectReason").value("image is blurry"));

        mockMvc.perform(put("/api/users/31/certification/audit")
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

        verify(userService).getUserCertificationDetail(31L);
        verify(userService).auditUserCertification(eq(31L), any());
    }

    @Test
    void certificationDetailAndAudit_shouldRejectVolunteerRole() throws Exception {
        String token = "audit-certification-volunteer-token";
        mockAuthenticatedVolunteer(token, "user-volunteer");

        mockMvc.perform(get("/api/users/31/certification")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        mockMvc.perform(put("/api/users/31/certification/audit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        verify(userService, never()).getUserCertificationDetail(31L);
        verify(userService, never()).auditUserCertification(eq(31L), any());
    }

    @Test
    void certificationDetailAndAudit_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/users/31/certification"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        mockMvc.perform(put("/api/users/31/certification/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).getUserCertificationDetail(31L);
        verify(userService, never()).auditUserCertification(eq(31L), any());
    }

    @Test
    void updateMe_shouldKeepSubmitContractForAuthenticatedUser() throws Exception {
        String token = "user-update-me-token";
        mockAuthenticatedVolunteer(token, "volunteer-user");

        mockMvc.perform(put("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "volunteer-updated",
                                  "realName": "Volunteer Updated",
                                  "phone": "13900007777",
                                  "email": "volunteer-updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(userService).updateCurrentUser(any());
    }

    @Test
    void updateMe_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anonymous-updated",
                                  "realName": "Anonymous Updated",
                                  "phone": "13900007777",
                                  "email": "anonymous-updated@example.com"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).updateCurrentUser(any());
    }

    @Test
    void updatePassword_shouldKeepSubmitContractForAuthenticatedUser() throws Exception {
        String token = "user-update-password-token";
        mockAuthenticatedVolunteer(token, "volunteer-user");

        mockMvc.perform(put("/api/users/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "old-password",
                                  "newPassword": "new-password-123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("updated"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(userService).updatePassword(any());
    }

    @Test
    void updatePassword_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "old-password",
                                  "newPassword": "new-password-123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));

        verify(userService, never()).updatePassword(any());
    }

    private void mockAuthenticatedAdmin(String token, String username) {
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isTokenValid(token)).thenReturn(true);
        when(jwtTokenUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(username);
        UserBuilder builder = withUsername(username).password("ignored").roles("ADMIN");
        when(dbUserDetailsService.loadUserByUsername(username)).thenReturn(builder.build());
    }

    private void mockAuthenticatedVolunteer(String token, String username) {
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isTokenValid(token)).thenReturn(true);
        when(jwtTokenUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(username);
        UserBuilder builder = withUsername(username).password("ignored").roles("VOLUNTEER");
        when(dbUserDetailsService.loadUserByUsername(username)).thenReturn(builder.build());
    }

    private User user(Long id, String username, String phone, String email, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(1);
        user.setCertified("VOLUNTEER".equals(role) ? 1 : 0);
        user.setRealName("Display Name");
        user.setCertificationStatus("VOLUNTEER".equals(role) ? "PENDING" : null);
        return user;
    }

    private VolunteerCertification certification(Long id, Long userId, String status) {
        VolunteerCertification certification = new VolunteerCertification();
        certification.setId(id);
        certification.setUserId(userId);
        certification.setRealName("Volunteer User");
        certification.setIdCardNo("330101199001011234");
        certification.setIdCardFrontUrl("https://example.com/id-front.jpg");
        certification.setIdCardBackUrl("https://example.com/id-back.jpg");
        certification.setStatus(status);
        certification.setRejectReason("REJECTED".equals(status) ? "image is blurry" : null);
        return certification;
    }
}
