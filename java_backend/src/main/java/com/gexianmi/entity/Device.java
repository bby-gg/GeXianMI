package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备实体类
 *
 * <p>对应数据库表 devices，记录大棚内安装的各类传感器和监测设备信息，
 * 包括设备编号、名称、位置、运行状态及所属大棚等</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
@TableName("devices")
public class Device {

    /** 设备编号，手动指定（主键） */
    @TableId
    private String code;

    /** 设备名称 */
    private String name;

    /** 设备安装位置描述 */
    private String location;

    /** 设备运行状态（online-在线 / offline-离线 / maintenance-维护中） */
    private String status;

    /** 设备安装日期 */
    private LocalDate installedAt;

    /** 备注信息 */
    private String notes;

    /** 所属大棚ID */
    private Long greenhouseId;

    /** 逻辑删除标记（0-未删除 / 1-已删除） */
    @TableLogic
    private Integer isDeleted;

    /** 记录创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 记录更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 所属大棚名称（非数据库字段，由 Service 层填充） */
    @TableField(exist = false)
    private String greenhouseName;
}
