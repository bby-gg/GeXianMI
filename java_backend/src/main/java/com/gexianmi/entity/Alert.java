package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警记录实体类
 *
 * <p>对应数据库表 alerts，记录环境参数超出阈值时触发的告警信息，
 * 包括告警参数、当前值、阈值、级别和状态等</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
@TableName("alerts")
public class Alert {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备编号，关联采集设备 */
    private String deviceId;

    /** 所属大棚ID */
    private Long greenhouseId;

    /** 监测参数标识（如 waterTemp、ph、dissolvedOxygen 等） */
    private String param;

    /** 监测参数中文名称 */
    private String paramName;

    /** 当前监测值 */
    private BigDecimal value;

    /** 告警下限阈值 */
    private BigDecimal thresholdLow;

    /** 告警上限阈值 */
    private BigDecimal thresholdHigh;

    /** 告警级别（warning-警告 / error-错误） */
    private String level;

    /** 告警消息内容，描述告警详情 */
    private String message;

    /** 告警状态（active-活跃中 / confirmed-已确认 / ignored-已忽略） */
    private String status;

    /** 告警确认时间 */
    private LocalDateTime confirmedAt;

    /** 告警触发时间 */
    private LocalDateTime triggeredAt;

    /** 记录创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
