package com.gexianmi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 环境数据上报请求体DTO
 * <p>对应环境数据采集接口的JSON请求体，包含水温、pH、溶解氧、光照强度和电导率等指标</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
public class EnvironmentDataDTO {

    /** 设备编号 */
    @NotBlank(message = "设备编号不能为空")
    private String deviceId;

    /** 采集时间，格式: yyyy-MM-dd HH:mm:ss */
    @NotBlank(message = "采集时间不能为空")
    private String timestamp;

    /** 水温（°C），范围 0 ~ 60 */
    @NotNull(message = "水温不能为空")
    @DecimalMin(value = "0.0", message = "水温不能为负")
    @DecimalMax(value = "60.0", message = "水温超出合理范围")
    private BigDecimal waterTemp;

    /** pH值，范围 0 ~ 14 */
    @NotNull(message = "pH 值不能为空")
    @DecimalMin(value = "0.0", message = "pH 值不能为负")
    @DecimalMax(value = "14.0", message = "pH 值超出合理范围")
    private BigDecimal ph;

    /** 溶解氧（mg/L） */
    @NotNull(message = "溶解氧不能为空")
    @DecimalMin(value = "0.0", message = "溶解氧不能为负")
    private BigDecimal dissolvedOxygen;

    /** 光照强度（lux） */
    @NotNull(message = "光照强度不能为空")
    @DecimalMin(value = "0.0", message = "光照强度不能为负")
    private BigDecimal lightIntensity;

    /** 电导率（μS/cm） */
    @NotNull(message = "电导率不能为空")
    @DecimalMin(value = "0.0", message = "电导率不能为负")
    private BigDecimal conductivity;
}
