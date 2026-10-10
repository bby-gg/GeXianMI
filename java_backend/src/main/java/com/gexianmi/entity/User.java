package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * <p>对应数据库表 users，记录系统用户的基本信息，
 * 包括用户名、密码哈希、角色和最后登录时间等</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
@TableName("users")
public class User {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户登录名 */
    private String username;

    /** 用户密码哈希值，不存储明文密码 */
    private String passwordHash;

    /** 用户角色（admin-管理员 / operator-操作员 / viewer-查看者） */
    private String role;

    /** 用户最后一次登录时间 */
    private LocalDateTime lastLoginAt;

    /** 记录创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
