package com.gexianmi.controller;

import com.gexianmi.common.Result;
import com.gexianmi.config.RequireAdmin;
import com.gexianmi.dto.LoginDTO;
import com.gexianmi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户认证接口控制器
 * <p>提供登录、注册、密码修改等认证相关操作</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录，校验用户名和密码并返回令牌
     *
     * @param dto 登录请求，包含用户名和密码
     * @return 登录结果，包含用户信息和令牌
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        try {
            return Result.ok(authService.login(dto.getUsername(), dto.getPassword()));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    /**
     * 注册新用户（仅管理员可操作）
     *
     * @param dto 注册请求，包含用户名和密码
     * @return 注册结果
     */
    @RequireAdmin
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody LoginDTO dto) {
        try {
            authService.register(dto.getUsername(), dto.getPassword());
            return Result.ok("注册成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<?> me(@RequestParam Long userId) {
        return Result.ok(authService.getCurrentUser(userId));
    }

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     * @return 操作结果
     */
    @PutMapping("/password")
    public Result<?> changePassword(
            @RequestParam Long userId,
            @RequestParam String oldPwd,
            @RequestParam String newPwd) {
        try {
            boolean ok = authService.changePassword(userId, oldPwd, newPwd);
            return ok ? Result.ok("密码修改成功") : Result.error("修改失败");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
