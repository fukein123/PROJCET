package com.community.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入志愿者姓名")
    private String realName;

    @NotBlank(message = "请输入密码")
    private String password;

    @NotBlank(message = "请输入确认密码")
    private String confirmPassword;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "请输入手机号")
    private String phone;

    private String gender;
    private String avatar;
}
