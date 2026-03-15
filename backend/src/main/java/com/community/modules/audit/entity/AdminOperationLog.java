package com.community.modules.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_operation_log")
public class AdminOperationLog extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;
    private String operatorUsername;
    private String actionType;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String result;
    private String detail;
}
