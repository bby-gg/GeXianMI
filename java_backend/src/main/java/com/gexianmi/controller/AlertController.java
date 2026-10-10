package com.gexianmi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gexianmi.common.Result;
import com.gexianmi.entity.Alert;
import com.gexianmi.service.AlertService;
import com.gexianmi.service.AlertSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

/**
 * 告警管理接口控制器
 * <p>提供告警列表查询、确认和忽略等操作</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AlertSseService alertSseService;

    /**
     * 查询告警列表，支持按状态、设备ID筛选和数量限制
     *
     * @param status   告警状态（可选）
     * @param deviceId 设备编号（可选）
     * @param limit    返回条数上限（可选）
     * @return 告警列表
     */
    @GetMapping
    public Result<Page<Alert>> listAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(alertService.listAll(status, deviceId, page, size));
    }

    /**
     * 查询所有活跃（未处理）告警
     *
     * @param deviceId 设备编号（可选）
     * @return 活跃告警列表
     */
    @GetMapping("/active")
    public Result<List<Alert>> listActive(@RequestParam(required = false) String deviceId) {
        return Result.ok(alertService.listActive(deviceId));
    }

    /**
     * 订阅告警实时推送（SSE）。
     * <p>前端 EventSource 连接此端点，有新告警时后端主动推送 "new-alert" 事件。</p>
     *
     * @return 长连接的 SseEmitter
     */
    @GetMapping("/stream")
    public SseEmitter stream() {
        return alertSseService.subscribe();
    }

    /**
     * 确认单条告警，将其标记为已确认状态
     *
     * @param id 告警ID
     * @return 操作结果
     */
    @PostMapping("/{id}/confirm")
    public Result<?> confirm(@PathVariable Long id) {
        return alertService.confirm(id) ? Result.ok("已确认") : Result.error("确认失败");
    }

    /**
     * 忽略单条告警，将其标记为已忽略状态
     *
     * @param id 告警ID
     * @return 操作结果
     */
    @PostMapping("/{id}/ignore")
    public Result<?> ignore(@PathVariable Long id) {
        return alertService.ignore(id) ? Result.ok("已忽略") : Result.error("忽略失败");
    }

    /**
     * 批量确认所有告警
     *
     * @return 操作结果，包含确认数量
     */
    @PostMapping("/confirm-all")
    public Result<?> confirmAll() {
        int count = alertService.confirmAll();
        return Result.ok("已确认 " + count + " 条告警");
    }
}
