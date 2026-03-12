package com.community.modules.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "请选择结束时间")
    @Future(message = "结束时间必须晚于当前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotBlank(message = "请输入活动地址")
    private String address;

    @NotNull(message = "请输入目标人数")
    @Min(value = 1, message = "目标人数不能小于 1")
    private Integer targetCount;

    @NotNull(message = "请输入志愿者人数")
    @Min(value = 1, message = "志愿者人数不能小于 1")
    private Integer volunteerQuota;

    @NotBlank(message = "请输入活动内容")
    private String content;

    private String description;
    private String coverImage;
    private String status;
}
