package com.community.modules.audit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.web.PageResult;
import com.community.common.util.SecurityUtil;
import com.community.modules.audit.entity.AdminOperationLog;
import com.community.modules.audit.mapper.AdminOperationLogMapper;
import com.community.modules.user.entity.User;
import com.community.modules.user.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOperationLogService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final AdminOperationLogMapper adminOperationLogMapper;
    private final UserMapper userMapper;

    public void record(String actionType,
                       String targetType,
                       Long targetId,
                       String targetName,
                       String detail) {
        Long operatorId = SecurityUtil.currentUserId();
        if (operatorId == null || !ROLE_ADMIN.equalsIgnoreCase(SecurityUtil.currentRole())) {
            return;
        }

        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(operatorId);
        log.setOperatorUsername(resolveOperatorUsername(operatorId));
        log.setActionType(normalizeRequired(actionType));
        log.setTargetType(normalizeRequired(targetType));
        log.setTargetId(targetId);
        log.setTargetName(normalizeOptional(targetName));
        log.setResult("SUCCESS");
        log.setDetail(normalizeOptional(detail));
        adminOperationLogMapper.insert(log);
    }

    public List<AdminOperationLog> listRecent(int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 20));
        return adminOperationLogMapper.selectList(new LambdaQueryWrapper<AdminOperationLog>()
                .orderByDesc(AdminOperationLog::getCreateTime)
                .last("limit " + resolvedLimit));
    }

    public PageResult<AdminOperationLog> page(long current,
                                              long size,
                                              String actionType,
                                              String targetType,
                                              String result,
                                              String operatorKeyword,
                                              String targetKeyword) {
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<AdminOperationLog>()
                .eq(StringUtils.hasText(actionType), AdminOperationLog::getActionType, actionType.trim().toUpperCase())
                .eq(StringUtils.hasText(targetType), AdminOperationLog::getTargetType, targetType.trim().toUpperCase())
                .eq(StringUtils.hasText(result), AdminOperationLog::getResult, result.trim().toUpperCase())
                .and(StringUtils.hasText(operatorKeyword), query -> query
                        .like(AdminOperationLog::getOperatorUsername, operatorKeyword.trim()))
                .and(StringUtils.hasText(targetKeyword), query -> query
                        .like(AdminOperationLog::getTargetName, targetKeyword.trim())
                        .or()
                        .like(AdminOperationLog::getDetail, targetKeyword.trim()))
                .orderByDesc(AdminOperationLog::getCreateTime);
        PageHelper.startPage((int) current, (int) size);
        List<AdminOperationLog> records = adminOperationLogMapper.selectList(wrapper);
        PageInfo<AdminOperationLog> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    private String resolveOperatorUsername(Long operatorId) {
        User user = userMapper.selectById(operatorId);
        if (user == null || !StringUtils.hasText(user.getUsername())) {
            return SecurityUtil.currentUsername();
        }
        return user.getUsername();
    }

    private String normalizeRequired(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : "UNKNOWN";
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }
}
