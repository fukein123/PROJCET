package com.community.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Email(message = "Email format is invalid")
    private String email;

    private String phone;
    private String gender;
    private String avatar;
    private String realName;

    @Min(value = 0, message = "Status must be 0 or 1")
    @Max(value = 1, message = "Status must be 0 or 1")
    private Integer status;

    @Pattern(regexp = "ADMIN|VOLUNTEER", message = "Role must be ADMIN or VOLUNTEER")
    private String role;

    @Min(value = 0, message = "Certified must be 0 or 1")
    @Max(value = 1, message = "Certified must be 0 or 1")
    private Integer certified;
}
