package com.community.modules.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forum_post")
public class ForumPost extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String coverImage;
    private String summary;
    private String content;
    private Long categoryId;
    private Long userId;
    private String status;
    private Integer views;
    private String auditReason;
}
