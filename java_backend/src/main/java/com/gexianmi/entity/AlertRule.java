package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警规则配置实体类
 *
 * <p>对应数据库表 alert_rules，定义各监测参数的正常范围和告警阈值，
 * 系统根据此规则判断环境数据是否异常并产生告警</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
@TableName("alert_rules")
public class AlertRule {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 监测参数标识（如 waterTemp、ph、dissolvedOxygen、lightIntensity、conductivity） */
    private String param;

    /** 监测参数中文名称 */
    private String paramName;

    /** 参数计量单位 */
    private String unit;

    /** 正常范围下限值 */
    private BigDecimal minVal;

    /** 正常范围上限值 */
    private BigDecimal maxVal;

    /** 告警下限阈值，低于此值触发告警 */
    private BigDecimal thresholdLow;

    /** 告警上限阈值，高于此值触发告警 */
    private BigDecimal thresholdHigh;

    /** 告警级别（warning-警告 / error-错误） */
    private String level;

    /** 启用状态（0-禁用 / 1-启用） */
    private Integer enabled;

    /** 记录创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 记录更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
