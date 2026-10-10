package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexianmi.entity.UserGreenhouseAccess;
import com.gexianmi.mapper.UserGreenhouseAccessMapper;
import com.gexianmi.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 授权管理业务实现。
 *
 * @author GXM-Team
 * @since 2026-07-13
 */
@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {

    private final UserGreenhouseAccessMapper accessMapper;

    @Override
    public List<Long> getAuthorizedGreenhouseIds(Long userId) {
        LambdaQueryWrapper<UserGreenhouseAccess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserGreenhouseAccess::getUserId, userId);
        List<UserGreenhouseAccess> list = accessMapper.selectList(wrapper);
        return list.stream().map(UserGreenhouseAccess::getGreenhouseId).toList();
    }

    @Override
    public List<UserGreenhouseAccess> listByUser(Long userId) {
        LambdaQueryWrapper<UserGreenhouseAccess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserGreenhouseAccess::getUserId, userId);
        return accessMapper.selectList(wrapper);
    }

    /**
     * 全量覆盖授权：先清空旧授权，再插入新授权，保证原子性。
     */
    @Override
    @Transactional
    public void saveAccess(Long userId, List<Long> greenhouseIds) {
        // 1. 删掉该用户所有旧授权
        LambdaQueryWrapper<UserGreenhouseAccess> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserGreenhouseAccess::getUserId, userId);
        accessMapper.delete(deleteWrapper);

        // 2. 逐条插入新授权（BaseMapper.insert 不支持批量，循环逐条插）
        for (Long ghId : greenhouseIds) {
            UserGreenhouseAccess uga = new UserGreenhouseAccess();
            uga.setUserId(userId);
            uga.setGreenhouseId(ghId);
            accessMapper.insert(uga);
        }
    }

    @Override
    @Transactional
    public void grant(Long userId, List<Long> greenhouseIds) {
        List<Long> existing = getAuthorizedGreenhouseIds(userId);
        for (Long ghId : greenhouseIds) {
            if (!existing.contains(ghId)) {
                UserGreenhouseAccess uga = new UserGreenhouseAccess();
                uga.setUserId(userId);
                uga.setGreenhouseId(ghId);
                accessMapper.insert(uga);
            }
        }
    }

    @Override
    @Transactional
    public void revoke(Long userId, Long greenhouseId) {
        LambdaQueryWrapper<UserGreenhouseAccess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserGreenhouseAccess::getUserId, userId)
               .eq(UserGreenhouseAccess::getGreenhouseId, greenhouseId);
        accessMapper.delete(wrapper);
    }
}
