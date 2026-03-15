package com.community.modules.content.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MallProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 128, message = "Product name must be within 128 characters")
    private String name;

    @NotBlank(message = "Product image is required")
    @Size(max = 255, message = "Product image URL is too long")
    private String imageUrl;

    @NotBlank(message = "Product summary is required")
    @Size(max = 500, message = "Product summary must be within 500 characters")
    private String summary;

    @NotNull(message = "Points cost is required")
    @Min(value = 0, message = "Points cost must be greater than or equal to 0")
    private Integer pointsCost;

    @NotNull(message = "Product stock is required")
    @Min(value = 0, message = "Product stock must be greater than or equal to 0")
    private Integer stock;

    @Min(value = 0, message = "Product status must be 0 or 1")
    @Max(value = 1, message = "Product status must be 0 or 1")
    private Integer status;
}
