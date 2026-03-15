package com.community.modules.content.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExchangeOrderCreateRequest {

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotBlank(message = "Receiver name is required")
    @Size(max = 64, message = "Receiver name must be within 64 characters")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    @Size(max = 32, message = "Receiver phone must be within 32 characters")
    private String receiverPhone;

    @NotBlank(message = "Receiver address is required")
    @Size(max = 255, message = "Receiver address must be within 255 characters")
    private String receiverAddress;

    @NotBlank(message = "Request key is required")
    @Size(max = 64, message = "Request key must be within 64 characters")
    private String requestKey;
}
