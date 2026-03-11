package com.community.modules.activity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationAuditRequest {
    @NotBlank(message = "Audit status is required")
    private String status;
    private String rejectReason;
}

