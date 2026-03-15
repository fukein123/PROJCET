package com.community.modules.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivityApplyRequest {
    @NotBlank(message = "Apply reason is required")
    @Size(max = 500, message = "Apply reason must be at most 500 characters")
    private String applyReason;
}
