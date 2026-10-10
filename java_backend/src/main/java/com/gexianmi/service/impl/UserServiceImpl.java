package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexianmi.entity.User;
import com.gexianmi.mapper.UserMapper;
import com.gexianmi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 用户管理业务实现。
 *
 * @author GXM-Team
 * @since 2026-10-10
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 允许的角色集合 */
    private static final Set<String> ROLES = Set.of("admin", "operator", "viewer");

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public List<User> listUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(User::getId);
        return userMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void changeRole(Long userId, String role) {
        if (role == null || !ROLES.contains(role)) {
            throw new RuntimeException("无效的角色: " + role);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setRole(role);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("密码至少 6 位");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPasswordHash(encoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
