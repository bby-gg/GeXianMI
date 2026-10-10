package com.gexianmi.controller;

import com.gexianmi.common.Result;
import com.gexianmi.config.RequireAdmin;
import com.gexianmi.entity.Device;
import com.gexianmi.entity.Greenhouse;
import com.gexianmi.service.GreenhouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 大棚管理接口控制器
 * <p>提供大棚信息的增删改查及关联设备查询功能</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GreenhouseController {

    private final GreenhouseService greenhouseService;

    /**
     * 查询所有大棚
     *
     * @return 大棚列表
     */
    @GetMapping("/greenhouses")
    public Result<List<Greenhouse>> listAll() {
        return Result.ok(greenhouseService.listAll());
    }

    /**
     * 创建新大棚（仅管理员可操作）
     *
     * @param gh 大棚信息
     * @return 创建后的大棚
     */
    @RequireAdmin
    @PostMapping("/greenhouses")
    public Result<Greenhouse> create(@RequestBody Greenhouse gh) {
        return Result.ok(greenhouseService.create(gh));
    }

    /**
     * 更新指定大棚信息（仅管理员可操作）
     *
     * @param id 大棚ID
     * @param gh 更新后的大棚信息
     * @return 更新后的大棚
     */
    @RequireAdmin
    @PutMapping("/greenhouses/{id}")
    public Result<Greenhouse> update(@PathVariable Long id, @RequestBody Greenhouse gh) {
        return Result.ok(greenhouseService.update(id, gh));
    }

    /**
     * 删除指定大棚（仅管理员可操作）
     *
     * @param id 大棚ID
     * @return 操作结果
     */
    @RequireAdmin
    @DeleteMapping("/greenhouses/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean ok = greenhouseService.delete(id);
        return ok ? Result.ok("删除成功") : Result.error("删除失败");
    }

    /**
     * 查询指定大棚下的所有设备
     *
     * @param id 大棚ID
     * @return 设备列表
     */
    @GetMapping("/greenhouses/{id}/devices")
    public Result<List<Device>> getDevices(@PathVariable Long id) {
        return Result.ok(greenhouseService.getDevices(id));
    }
}
