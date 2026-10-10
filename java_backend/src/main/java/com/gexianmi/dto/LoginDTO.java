package com.gexianmi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录/注册请求体DTO
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
