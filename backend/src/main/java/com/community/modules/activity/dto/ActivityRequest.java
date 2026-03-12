package com.community.modules.activity.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityRequest {
    @NotBlank(message = "请输入活动标题")
    private String title;

    @NotNull(message = "请选择活动分类")
    private Long categoryId;

    @NotNull(message = "请选择开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "请选择结束时间")
    @Future(message = "结束时间必须晚于当前时间")
    private LocalDateTime endTime;

    @NotBlank(message = "请输入活动地址")
    private String address;

    @Min(value = 1, message = "目标人数不能小于 1")
    private Integer targetCount;

    private String description;
    private String coverImage;
    private String status;
    private Double latitude;
    private Double longitude;
}
