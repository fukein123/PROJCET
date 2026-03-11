package com.community.modules.activity.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @NotBlank(message = "Address is required")
    private String address;

    @Min(value = 1, message = "Target count must be >= 1")
    private Integer targetCount;

    private String description;
    private String status;
    private Double latitude;
    private Double longitude;
}

