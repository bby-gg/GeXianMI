package com.gexianmi.controller;

import com.gexianmi.common.Result;
import com.gexianmi.config.RequireAdmin;
import com.gexianmi.entity.Device;
import com.gexianmi.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 设备管理接口控制器
 * <p>提供设备的增删改查操作</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * 查询所有设备
     *
     * @return 设备列表
     */
    @GetMapping("/devices")
    public Result<List<Device>> listAll() {
        return Result.ok(deviceService.listAll());
    }

    /**
     * 创建新设备（仅管理员可操作）
     *
     * @param device 设备信息
     * @return 创建后的设备
     */
    @RequireAdmin
    @PostMapping("/devices")
    public Result<Device> create(@RequestBody Device device) {
        return Result.ok(deviceService.create(device));
    }

    /**
     * 更新指定设备（仅管理员可操作）
     *
     * @param code   设备编码
     * @param device 更新后的设备信息
     * @return 更新后的设备
     */
    @RequireAdmin
    @PutMapping("/devices/{code}")
    public Result<Device> update(@PathVariable String code, @RequestBody Device device) {
        return Result.ok(deviceService.update(code, device));
    }

    /**
     * 删除指定设备（仅管理员可操作）
     *
     * @param code 设备编码
     * @return 操作结果
     */
    @RequireAdmin
    @DeleteMapping("/devices/{code}")
    public Result<?> delete(@PathVariable String code) {
        boolean ok = deviceService.delete(code);
        return ok ? Result.ok("删除成功") : Result.error("删除失败");
    }
}
