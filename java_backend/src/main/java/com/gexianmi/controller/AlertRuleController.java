package com.gexianmi.controller;

import com.gexianmi.common.Result;
import com.gexianmi.config.RequireAdmin;
import com.gexianmi.entity.AlertRule;
import com.gexianmi.service.AlertRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 告警规则管理接口控制器
 * <p>提供告警规则的查询和更新操作</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@RestController
@RequestMapping("/api/alert-rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    /**
     * 获取所有告警规则
     *
     * @return 告警规则列表
     */
    @GetMapping
    public Result<List<AlertRule>> listAll() {
        return Result.ok(alertRuleService.listAll());
    }

    /**
     * 更新指定告警规则（仅管理员可操作）
     *
     * @param id   规则ID
     * @param rule 更新后的规则内容
     * @return 更新后的告警规则
     */
    @RequireAdmin
    @PutMapping("/{id}")
    public Result<AlertRule> update(@PathVariable Long id, @RequestBody AlertRule rule) {
        return Result.ok(alertRuleService.update(id, rule));
    }
}
