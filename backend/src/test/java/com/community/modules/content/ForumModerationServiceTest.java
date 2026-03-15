package com.community.modules.content;

import com.community.common.exception.BusinessException;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.mapper.ForumPostMapper;
import com.community.modules.content.service.ForumModerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForumModerationServiceTest {

    @Mock
    private ForumPostMapper forumPostMapper;
    @Mock
    private AdminOperationLogService adminOperationLogService;

    @InjectMocks
    private ForumModerationService forumModerationService;

    @Test
    void auditPost_shouldApprovePendingPost() {
        ForumPost post = new ForumPost();
        post.setId(41L);
        post.setTitle("Volunteer Story");
        post.setStatus("PENDING");
        when(forumPostMapper.selectById(41L)).thenReturn(post);

        forumModerationService.auditPost(41L, "APPROVED", null);

        ArgumentCaptor<ForumPost> postCaptor = ArgumentCaptor.forClass(ForumPost.class);
        verify(forumPostMapper).updateById(postCaptor.capture());
        assertEquals("APPROVED", postCaptor.getValue().getStatus());
        assertEquals(null, postCaptor.getValue().getAuditReason());
        verify(adminOperationLogService).record("AUDIT_POST", "POST", 41L, "Volunteer Story", "status=APPROVED");
    }

    @Test
    void auditPost_shouldRejectNonPendingPost() {
        ForumPost post = new ForumPost();
        post.setId(42L);
        post.setTitle("Archived Story");
        post.setStatus("APPROVED");
        when(forumPostMapper.selectById(42L)).thenReturn(post);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> forumModerationService.auditPost(42L, "REJECTED", "duplicate")
        );

        assertEquals("Only pending posts can be audited", ex.getMessage());
        verify(forumPostMapper, never()).updateById(any(ForumPost.class));
    }

    @Test
    void auditPost_shouldRequireRejectReasonWhenRejected() {
        ForumPost post = new ForumPost();
        post.setId(43L);
        post.setTitle("Need Review");
        post.setStatus("PENDING");
        when(forumPostMapper.selectById(43L)).thenReturn(post);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> forumModerationService.auditPost(43L, "REJECTED", " ")
        );

        assertEquals("Reject reason is required when rejecting a post", ex.getMessage());
        verify(forumPostMapper, never()).updateById(any(ForumPost.class));
    }
}
