package com.gexianmi.controller;

import com.gexianmi.common.Result;
import com.gexianmi.config.RequireAdmin;
import com.gexianmi.entity.User;
import com.gexianmi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理接口（仅管理员可操作）。
 *
 * @author GXM-Team
 * @since 2026-10-10
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 查询全部用户列表 */
    @RequireAdmin
    @GetMapping
    public Result<List<User>> listUsers() {
        return Result.ok(userService.listUsers());
    }

    /** 调整用户角色 */
    @RequireAdmin
    @PutMapping("/{id}/role")
    public Result<?> changeRole(@PathVariable Long id, @RequestParam String role) {
        userService.changeRole(id, role);
        return Result.ok("角色已更新");
    }

    /** 重置用户密码 */
    @RequireAdmin
    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("newPassword"));
        return Result.ok("密码已重置");
    }
}
