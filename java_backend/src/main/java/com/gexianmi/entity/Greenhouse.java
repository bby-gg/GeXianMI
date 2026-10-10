package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 大棚/养殖基地实体类
 *
 * <p>对应数据库表 greenhouses，记录葛仙米养殖大棚的基本信息，
 * 包括编号、名称、位置、面积和运营状态等</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
@TableName("greenhouses")
public class Greenhouse {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 大棚编号 */
    private String code;

    /** 大棚名称 */
    private String name;

    /** 大棚所在位置 */
    private String location;

    /** 大棚运营状态（normal-正常运营 / maintenance-维护中 / closed-已关闭） */
    private String status;

    /** 大棚面积，单位为平方米 */
    private BigDecimal area;

    /** 备注信息 */
    private String notes;

    /** 逻辑删除标记（0-未删除 / 1-已删除） */
    @TableLogic
    private Integer isDeleted;

    /** 记录创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 记录更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 所属大棚下的设备数量（非数据库字段，由 Service 层填充） */
    @TableField(exist = false)
    private Integer deviceCount;
}
