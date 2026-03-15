package com.community.modules.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mall_product")
public class MallProduct extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String imageUrl;
    private String summary;
    private Integer pointsCost;
    private Integer stock;
    private Integer status;
}
