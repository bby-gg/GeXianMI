package com.gexianmi.service;

import com.gexianmi.entity.User;
import java.util.Map;

/**
 * 认证业务接口，定义用户登录、注册及密码管理等身份认证操作。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
public interface AuthService {

    /**
     * 用户登录认证，校验用户名和密码并签发JWT令牌。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 包含token和userInfo的认证结果Map
     * @throws RuntimeException 用户名或密码错误时抛出
     */
    Map<String, Object> login(String username, String password);

    /**
     * 注册新用户，校验用户名唯一性后创建账号。
     *
     * @param username 用户名
     * @param password 明文密码（至少6位）
     * @throws RuntimeException 用户名已存在或密码不足6位时抛出
     */
    void register(String username, String password);

    /**
     * 根据用户ID查询当前用户信息。
     *
     * @param userId 用户ID
     * @return 用户实体对象，不存在返回null
     */
    User getCurrentUser(Long userId);

    /**
     * 修改用户密码，需校验旧密码正确性。
     *
     * @param userId 用户ID
     * @param oldPwd 旧密码（明文）
     * @param newPwd 新密码（明文）
     * @return true表示修改成功，false表示失败
     * @throws RuntimeException 旧密码错误时抛出
     */
    boolean changePassword(Long userId, String oldPwd, String newPwd);
}
