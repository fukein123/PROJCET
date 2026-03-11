package com.community.modules.activity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignRequest {
    @NotNull(message = "Application id is required")
    private Long applicationId;
    @NotNull(message = "Latitude is required")
    private Double latitude;
    @NotNull(message = "Longitude is required")
    private Double longitude;
}

