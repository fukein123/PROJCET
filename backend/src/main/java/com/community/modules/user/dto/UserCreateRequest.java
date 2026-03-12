package com.community.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    @NotBlank(message = "志愿者姓名不能为空")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String gender;
    private String avatar;

    @Min(value = 0, message = "状态值只能是 0 或 1")
    @Max(value = 1, message = "状态值只能是 0 或 1")
    private Integer status;

    @Min(value = 0, message = "认证状态只能是 0 或 1")
    @Max(value = 1, message = "认证状态只能是 0 或 1")
    private Integer certified;
}
