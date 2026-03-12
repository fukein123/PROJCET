package com.community.modules.content.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotNull(message = "请选择活动")
    private Long activityId;
    private String note;
    private String tag;
    private Integer priority;
}
