package com.community.modules.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class Activity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Long categoryId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String address;
    private String status;
    private Integer targetCount;
    private Integer volunteerQuota;
    private Integer pointReward;
    private String content;
    private String description;
    private String coverImage;
    private Long creatorId;
}
