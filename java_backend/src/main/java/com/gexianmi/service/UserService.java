package com.gexianmi.service;

import com.gexianmi.entity.User;

import java.util.List;

/**
 * 用户管理业务接口，提供用户列表、角色调整与密码重置能力（仅管理员使用）。
 *
 * @author GXM-Team
 * @since 2026-10-10
 */
public interface UserService {

    /** 查询全部用户（不含密码哈希） */
    List<User> listUsers();

    /** 调整用户角色（admin / operator / viewer） */
    void changeRole(Long userId, String role);

    /** 重置用户密码（管理员强制设置新密码） */
    void resetPassword(Long userId, String newPassword);
}
