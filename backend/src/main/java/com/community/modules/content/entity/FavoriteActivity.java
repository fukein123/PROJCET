package com.community.modules.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("favorite_activity")
public class FavoriteActivity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long activityId;
    private String activityTitle;
    private String activityAddress;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private String note;
    private String tag;
    private Integer priority;
}
