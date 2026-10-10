package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexianmi.entity.User;
import com.gexianmi.mapper.UserMapper;
import com.gexianmi.service.AuthService;
import com.gexianmi.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证业务实现，处理登录校验、JWT签发、用户注册及密码管理等操作。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** JWT令牌工具 */
    private final JwtUtil jwtUtil;
    /** BCrypt密码加密器 */
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成JWT令牌（包含 userId，后续权限过滤用）
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        ));
        return result;
    }

    @Override
    @Transactional
    public void register(String username, String password) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 密码长度校验，至少6位
        if (password == null || password.length() < 6) {
            throw new RuntimeException("密码至少 6 位");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));
        // 新注册用户默认分配操作员角色
        user.setRole("operator");
        // createdAt字段由MyBatis-Plus MetaObjectHandler自动填充
        userMapper.insert(user);
    }

    @Override
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPwd, String newPwd) {
        User user = userMapper.selectById(userId);
        if (user == null || !encoder.matches(oldPwd, user.getPasswordHash())) {
            throw new RuntimeException("旧密码错误");
        }
        user.setPasswordHash(encoder.encode(newPwd));
        return userMapper.updateById(user) > 0;
    }
}
