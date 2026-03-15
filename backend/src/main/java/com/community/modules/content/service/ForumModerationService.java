package com.community.modules.content.service;

import com.community.common.exception.BusinessException;
import com.community.modules.audit.service.AdminOperationLogService;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.mapper.ForumPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ForumModerationService {

    private final ForumPostMapper forumPostMapper;
    private final AdminOperationLogService adminOperationLogService;

    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long id, String status, String reason) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "Post not found");
        }

        String nextStatus = normalizeAuditStatus(status);
        String currentStatus = normalizePostStatus(post.getStatus());
        if (!"PENDING".equals(currentStatus)) {
            throw new BusinessException("Only pending posts can be audited");
        }
        if ("REJECTED".equals(nextStatus) && !StringUtils.hasText(reason)) {
            throw new BusinessException("Reject reason is required when rejecting a post");
        }

        post.setStatus(nextStatus);
        post.setAuditReason(StringUtils.hasText(reason) ? reason.trim() : null);
        forumPostMapper.updateById(post);
        adminOperationLogService.record(
                "AUDIT_POST",
                "POST",
                post.getId(),
                post.getTitle(),
                "status=" + nextStatus + ("REJECTED".equals(nextStatus) ? ", reason=" + post.getAuditReason() : "")
        );
    }

    private String normalizeAuditStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BusinessException("Audit status is required");
        }
        String normalized = status.trim().toUpperCase();
        if (!"APPROVED".equals(normalized) && !"REJECTED".equals(normalized)) {
            throw new BusinessException("Audit status must be APPROVED or REJECTED");
        }
        return normalized;
    }

    private String normalizePostStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "PENDING";
        }
        return status.trim().toUpperCase();
    }
}
