package com.community.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VolunteerCertificationSubmitRequest {
    @NotBlank(message = "Real name is required")
    private String realName;

    @NotBlank(message = "ID card number is required")
    private String idCardNo;

    @NotBlank(message = "ID card front image is required")
    private String idCardFrontUrl;

    @NotBlank(message = "ID card back image is required")
    private String idCardBackUrl;
}
