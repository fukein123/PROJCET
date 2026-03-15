package com.community.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VolunteerCertificationAuditRequest {
    @NotBlank(message = "Status is required")
    private String status;

    private String rejectReason;
}
