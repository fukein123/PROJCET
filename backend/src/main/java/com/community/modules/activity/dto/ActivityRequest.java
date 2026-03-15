package com.community.modules.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityRequest {
    @NotBlank(message = "Activity title is required")
    private String title;

    @NotNull(message = "Activity category is required")
    private Long categoryId;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotBlank(message = "Activity address is required")
    private String address;

    @NotNull(message = "Target count is required")
    @Min(value = 1, message = "Target count must be greater than 0")
    private Integer targetCount;

    @NotNull(message = "Volunteer quota is required")
    @Min(value = 1, message = "Volunteer quota must be greater than 0")
    private Integer volunteerQuota;

    @NotNull(message = "Point reward is required")
    @Min(value = 0, message = "Point reward must be greater than or equal to 0")
    private Integer pointReward;

    @NotBlank(message = "Activity content is required")
    private String content;

    private String description;
    private String coverImage;
    private String status;
}
