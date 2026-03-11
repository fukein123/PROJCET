package com.community.modules.activity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
}

