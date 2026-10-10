package com.gexianmi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexianmi.common.Result;
import com.gexianmi.common.UserContext;
import com.gexianmi.config.RequireAdmin;
import com.gexianmi.entity.Greenhouse;
import com.gexianmi.entity.User;
import com.gexianmi.mapper.GreenhouseMapper;
import com.gexianmi.mapper.UserMapper;
import com.gexianmi.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 授权管理接口（仅管理员可操作）。
 *
 * @author GXM-Team
 * @since 2026-07-13
 */
@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;
    private final UserMapper userMapper;
    private final GreenhouseMapper greenhouseMapper;

    /**
     * 获取所有操作员列表（供授权页左侧列表使用）。
     * GET /api/access/operators
     */
    @RequireAdmin
    @GetMapping("/operators")
    public Result<List<User>> listOperators() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "operator")
               .orderByAsc(User::getId);
        return Result.ok(userMapper.selectList(wrapper));
    }

    /**
     * 获取某个操作员的授权大棚ID列表。
     * GET /api/access/user/{userId}/greenhouses
     */
    @RequireAdmin
    @GetMapping("/user/{userId}/greenhouses")
    public Result<List<Long>> getUserGreenhouses(@PathVariable Long userId) {
        return Result.ok(accessService.getAuthorizedGreenhouseIds(userId));
    }

    /**
     * 获取所有大棚列表（供穿梭框/选择器使用）。
     * GET /api/access/greenhouses
     */
    @RequireAdmin
    @GetMapping("/greenhouses")
    public Result<List<Greenhouse>> listGreenhouses() {
        return Result.ok(greenhouseMapper.selectList(null));
    }

    /**
     * 批量保存授权：传入 userId + greenhouseIds，全量覆盖。
     * PUT /api/access/user/{userId}
     */
    @RequireAdmin
    @PutMapping("/user/{userId}")
    public Result<?> saveAccess(@PathVariable Long userId, @RequestBody List<Long> greenhouseIds) {
        accessService.saveAccess(userId, greenhouseIds != null ? greenhouseIds : List.of());
        return Result.ok("授权已更新");
    }

    /**
     * 获取当前登录用户被授权的大棚ID列表（所有角色通用）。
     * 管理员返回 null 表示「全部可见」。
     * GET /api/access/my-greenhouses
     */
    @GetMapping("/my-greenhouses")
    public Result<Map<String, Object>> myGreenhouses() {
        Map<String, Object> result = new HashMap<>();
        if (UserContext.isAdmin()) {
            result.put("all", true);       // 管理员：全部可见
            result.put("greenhouseIds", null);
        } else {
            result.put("all", false);
            result.put("greenhouseIds", accessService.getAuthorizedGreenhouseIds(UserContext.userId()));
        }
        return Result.ok(result);
    }
}
