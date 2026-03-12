package com.community.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private String gender;
    private String avatar;
    private String realName;

    @Min(value = 0, message = "状态值只能是 0 或 1")
    @Max(value = 1, message = "状态值只能是 0 或 1")
    private Integer status;

    @Pattern(regexp = "ADMIN|VOLUNTEER", message = "角色值只能是 ADMIN 或 VOLUNTEER")
    private String role;

    @Min(value = 0, message = "认证状态只能是 0 或 1")
    @Max(value = 1, message = "认证状态只能是 0 或 1")
    private Integer certified;
}
