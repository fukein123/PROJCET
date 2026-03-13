package com.community.modules.content;

import com.community.common.config.JwtAuthenticationFilter;
import com.community.common.config.SecurityConfig;
import com.community.common.exception.ApiErrorCode;
import com.community.common.exception.GlobalExceptionHandler;
import com.community.common.util.JwtTokenUtil;
import com.community.common.web.PageResult;
import com.community.modules.auth.service.DbUserDetailsService;
import com.community.modules.content.controller.ContentController;
import com.community.modules.content.entity.CommentInfo;
import com.community.modules.content.service.ContentService;
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

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ContentController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class ContentControllerCommentIT {

    private static final String COMMENT_PAGE_API = "/api/content/comments/page";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContentService contentService;

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private DbUserDetailsService dbUserDetailsService;

    @Test
    void pageComments_shouldSupportTargetTypeAndTargetIdFilterForPublicRequests() throws Exception {
        when(contentService.pageComments(1L, 5L, false, "POST", 11L, false))
                .thenReturn(pageResult(1, 5, comment(101L, "POST", 11L, 7L, "post comment")));

        mockMvc.perform(get(COMMENT_PAGE_API)
                        .param("current", "1")
                        .param("size", "5")
                        .param("targetType", "POST")
                        .param("targetId", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(101))
                .andExpect(jsonPath("$.data.records[0].targetType").value("POST"))
                .andExpect(jsonPath("$.data.records[0].targetId").value(11));

        verify(contentService).pageComments(1L, 5L, false, "POST", 11L, false);
    }

    @Test
    void pageComments_shouldKeepSupportingTargetTypeOnlyFilter() throws Exception {
        when(contentService.pageComments(1L, 200L, false, "POST", null, false))
                .thenReturn(pageResult(1, 200, comment(102L, "POST", 21L, 8L, "forum comment")));

        mockMvc.perform(get(COMMENT_PAGE_API)
                        .param("current", "1")
                        .param("size", "200")
                        .param("targetType", "POST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].targetType").value("POST"));

        verify(contentService).pageComments(1L, 200L, false, "POST", null, false);
    }

    @Test
    void pageComments_shouldRejectTargetIdWithoutTargetType() throws Exception {
        mockMvc.perform(get(COMMENT_PAGE_API)
                        .param("targetId", "22"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.REQUEST_INVALID.getCode()))
                .andExpect(jsonPath("$.message").value("\u6309\u76ee\u6807ID\u7b5b\u9009\u8bc4\u8bba\u65f6\u5fc5\u987b\u540c\u65f6\u63d0\u4f9b\u76ee\u6807\u7c7b\u578b"));

        verifyNoInteractions(contentService);
    }

    @Test
    void pageComments_shouldRejectOnlyMineForAnonymousRequests() throws Exception {
        mockMvc.perform(get(COMMENT_PAGE_API)
                        .param("onlyMine", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value("\u67e5\u8be2\u6211\u7684\u8bc4\u8bba\u524d\u8bf7\u5148\u767b\u5f55"));

        verifyNoInteractions(contentService);
    }

    @Test
    void pageComments_shouldAllowOnlyMineForAuthenticatedRequests() throws Exception {
        String token = "mock-token";
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isTokenValid(token)).thenReturn(true);
        when(jwtTokenUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("volunteer");
        when(dbUserDetailsService.loadUserByUsername("volunteer"))
                .thenReturn(User.withUsername("volunteer").password("ignored").roles("VOLUNTEER").build());
        when(contentService.pageComments(2L, 10L, true, "ACTIVITY", 31L, false))
                .thenReturn(pageResult(2, 10, comment(103L, "ACTIVITY", 31L, 9L, "my activity comment")));

        mockMvc.perform(get(COMMENT_PAGE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "2")
                        .param("size", "10")
                        .param("onlyMine", "true")
                        .param("targetType", "ACTIVITY")
                        .param("targetId", "31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.records[0].userId").value(9));

        verify(contentService).pageComments(2L, 10L, true, "ACTIVITY", 31L, false);
    }

    @Test
    void pageComments_shouldIgnoreIncludeTestDataForAnonymousRequests() throws Exception {
        when(contentService.pageComments(1L, 10L, false, "POST", 31L, false))
                .thenReturn(pageResult(1, 10, comment(104L, "POST", 31L, 2L, "public comment")));

        mockMvc.perform(get(COMMENT_PAGE_API)
                        .param("targetType", "POST")
                        .param("targetId", "31")
                        .param("includeTestData", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").value(104));

        verify(contentService).pageComments(1L, 10L, false, "POST", 31L, false);
    }

    @Test
    void pageComments_shouldAllowAdminToIncludeTestData() throws Exception {
        String token = "admin-token";
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isTokenValid(token)).thenReturn(true);
        when(jwtTokenUtil.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("admin");
        when(dbUserDetailsService.loadUserByUsername("admin"))
                .thenReturn(User.withUsername("admin").password("ignored").roles("ADMIN").build());

        CommentInfo smokeComment = comment(105L, "POST", 41L, 1L, "test comment");
        smokeComment.setTestDataTag("smoke:comment-it");
        when(contentService.pageComments(1L, 20L, false, "POST", 41L, true))
                .thenReturn(pageResult(1, 20, smokeComment));

        mockMvc.perform(get(COMMENT_PAGE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("current", "1")
                        .param("size", "20")
                        .param("targetType", "POST")
                        .param("targetId", "41")
                        .param("includeTestData", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].testDataTag").value("smoke:comment-it"));

        verify(contentService).pageComments(1L, 20L, false, "POST", 41L, true);
    }

    private PageResult<CommentInfo> pageResult(long current, long pageSize, CommentInfo comment) {
        return new PageResult<>(1, current, pageSize, List.of(comment));
    }

    private CommentInfo comment(Long id, String targetType, Long targetId, Long userId, String content) {
        CommentInfo comment = new CommentInfo();
        comment.setId(id);
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setStatus(1);
        return comment;
    }
}
