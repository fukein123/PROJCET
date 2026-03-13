package com.community.modules.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment_info")
public class CommentInfo extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String targetType;
    private Long targetId;
    private Long userId;
    private String content;
    private String testDataTag;
    private Integer status;
}
