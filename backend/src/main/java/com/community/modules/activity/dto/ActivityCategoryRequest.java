package com.community.modules.activity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityCategoryRequest {
    @NotBlank(message = "请输入分类名称")
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
}
