package com.community.modules.activity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationAuditRequest {
    @NotBlank(message = "请选择审核状态")
    private String status;
    private String rejectReason;
}
