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
@TableName("info_dynamic")
public class InfoDynamic extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String source;
    private String content;
    private String imageUrl;
    private String type;
    private Integer views;
    private Integer status;
    private LocalDateTime publishTime;
    private Long authorId;
}
