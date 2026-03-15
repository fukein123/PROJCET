package com.community.modules.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExchangeOrderStatusRequest {

    @NotBlank(message = "Order status is required")
    @Size(max = 32, message = "Order status must be within 32 characters")
    private String status;

    @Size(max = 255, message = "Order reason must be within 255 characters")
    private String reason;
}
