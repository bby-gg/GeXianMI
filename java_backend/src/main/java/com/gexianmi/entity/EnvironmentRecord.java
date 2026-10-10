package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 环境数据记录实体类
 *
 * <p>对应数据库表 environment_records，存储传感器定时采集的环境参数数据，
 * 包括水温、pH值、溶解氧、光照强度和电导率等关键水质指标</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
@TableName("environment_records")
public class EnvironmentRecord {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备编号，标识数据来源于哪个传感器 */
    private String deviceId;

    /** 传感器采集时间（前端字段名为 timestamp） */
    @JsonProperty("timestamp")
    private LocalDateTime recordedAt;

    /** 水温，单位为摄氏度（°C） */
    private BigDecimal waterTemp;

    /** pH值，反映水体酸碱度 */
    private BigDecimal ph;

    /** 溶解氧浓度，单位为毫克每升（mg/L） */
    private BigDecimal dissolvedOxygen;

    /** 光照强度，单位为勒克斯（lux） */
    private BigDecimal lightIntensity;

    /** 电导率，单位为微西门子每厘米（μS/cm） */
    private BigDecimal conductivity;

    /** 记录入库时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
