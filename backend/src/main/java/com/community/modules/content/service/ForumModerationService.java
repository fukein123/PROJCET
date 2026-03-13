package com.community.modules.content.service;

import com.community.common.exception.BusinessException;
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

    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long id, String status, String reason) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        String nextStatus = normalizeAuditStatus(status);
        String currentStatus = normalizePostStatus(post.getStatus());
        if (!"PENDING".equals(currentStatus)) {
            throw new BusinessException("仅待审核状态的帖子可执行审核操作");
        }
        if ("REJECTED".equals(nextStatus) && !StringUtils.hasText(reason)) {
            throw new BusinessException("驳回帖子时必须填写审核意见");
        }

        post.setStatus(nextStatus);
        post.setAuditReason(StringUtils.hasText(reason) ? reason.trim() : null);
        forumPostMapper.updateById(post);
    }

    private String normalizeAuditStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BusinessException("审核状态不能为空");
        }
        String normalized = status.trim().toUpperCase();
        if (!"APPROVED".equals(normalized) && !"REJECTED".equals(normalized)) {
            throw new BusinessException("审核状态只能是 APPROVED 或 REJECTED");
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
