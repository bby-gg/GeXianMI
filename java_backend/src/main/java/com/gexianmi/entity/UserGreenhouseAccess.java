package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户-大棚授权实体类。
 * <p>
 * 对应数据库表 user_greenhouse_access，记录操作员与大棚的多对多绑定关系。
 * 管理员（role=admin）不经过此表，默认拥有全部大棚的数据权限。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-07-13
 */
@Data
@TableName("user_greenhouse_access")
public class UserGreenhouseAccess {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，关联 users.id */
    private Long userId;

    /** 大棚ID，关联 greenhouses.id */
    private Long greenhouseId;

    /** 授权时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
