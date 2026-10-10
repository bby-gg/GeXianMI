package com.gexianmi.controller;

import com.gexianmi.common.Result;
import com.gexianmi.dto.EnvironmentDataDTO;
import com.gexianmi.entity.EnvironmentRecord;
import com.gexianmi.service.EnvironmentService;
import jakarta.validation.Valid;
import com.gexianmi.entity.Device;
import com.gexianmi.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 环境数据接口控制器
 * <p>提供环境数据的采集、查询和历史数据检索功能</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/api/environment")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;
    private final DeviceService deviceService;

    /**
     * 接收并保存设备上报的环境数据
     *
     * @param dto 环境数据请求体，包含水温、pH、溶解氧等指标
     * @return 保存结果
     */
    @PostMapping("/data")
    public Result<Void> receiveData(@Valid @RequestBody EnvironmentDataDTO dto) {
        environmentService.saveData(dto);
        return Result.ok("数据已保存");
    }

    /**
     * 查询指定设备最新一条环境数据
     *
     * @param deviceId 设备编号
     * @return 最新环境数据记录
     */
    @GetMapping("/latest")
    public Result<EnvironmentRecord> getLatest(@RequestParam String deviceId) {
        EnvironmentRecord record = environmentService.getLatest(deviceId);
        return Result.ok(record);
    }

    /**
     * 查询指定设备最近N条环境数据，用于曲线图展示
     *
     * @param deviceId 设备编号
     * @param limit    返回条数上限，默认100
     * @return 环境数据记录列表
     */
    @GetMapping("/recent")
    public Result<List<EnvironmentRecord>> getRecent(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "100") int limit) {
        List<EnvironmentRecord> records = environmentService.getRecent(deviceId, limit);
        return Result.ok(records);
    }

    /**
     * 按时间范围查询历史环境数据
     *
     * @param deviceId 设备编号
     * @param start    起始时间（可选，格式: yyyy-MM-ddTHH:mm:ss）
     * @param end      结束时间（可选，格式: yyyy-MM-ddTHH:mm:ss）
     * @return 历史环境数据记录列表
     */
    @GetMapping("/history")
    public Result<List<EnvironmentRecord>> getHistory(
            @RequestParam String deviceId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        List<EnvironmentRecord> records = environmentService.getHistory(deviceId, start, end);
        return Result.ok(records);
    }

    /**
     * 查询设备数据的时间范围（最早和最晚采集时间）
     *
     * @param deviceId 设备编号
     * @return 包含起始时间和结束时间的Map
     */
    @GetMapping("/range")
    public Result<Map<String, Object>> getDataRange(@RequestParam String deviceId) {
        return Result.ok(environmentService.getDataRange(deviceId));
    }

    /**
     * 获取所有设备列表，供前端下拉选择使用
     *
     * @return 设备列表
     */
    @GetMapping("/devices")
    public Result<List<Device>> listDevices() {
        return Result.ok(deviceService.listAll());
    }
}
