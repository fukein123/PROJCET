package com.community.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_change_log")
public class PointsChangeLog extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String changeNo;
    private Long userId;
    private String changeDirection;
    private Integer deltaPoints;
    private String sourceType;
    private Long sourceId;
    private String referenceNo;
    private String note;
}
