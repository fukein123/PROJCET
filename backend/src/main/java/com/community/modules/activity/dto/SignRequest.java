package com.community.modules.activity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignRequest {
    @NotNull(message = "申请编号不能为空")
    private Long applicationId;
    @NotNull(message = "纬度不能为空")
    private Double latitude;
    @NotNull(message = "经度不能为空")
    private Double longitude;
}
