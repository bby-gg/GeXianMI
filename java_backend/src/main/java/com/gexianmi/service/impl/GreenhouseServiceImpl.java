package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexianmi.common.UserContext;
import com.gexianmi.entity.Device;
import com.gexianmi.entity.Greenhouse;
import com.gexianmi.mapper.DeviceMapper;
import com.gexianmi.mapper.GreenhouseMapper;
import com.gexianmi.service.AccessService;
import com.gexianmi.service.GreenhouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 大棚业务实现，提供大棚增删改查及关联设备数量统计功能。
 * <p>
 * 非管理员用户仅能看到被授权大棚的数据。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Service
@RequiredArgsConstructor
public class GreenhouseServiceImpl implements GreenhouseService {

    /** 大棚数据访问层 */
    private final GreenhouseMapper greenhouseMapper;
    /** 设备数据访问层 */
    private final DeviceMapper deviceMapper;
    /** 授权管理服务 */
    private final AccessService accessService;

    @Override
    public List<Greenhouse> listAll() {
        List<Greenhouse> list;
        // 管理员：查全部；操作员/观察者：只查授权大棚
        if (UserContext.isAdmin()) {
            list = greenhouseMapper.selectList(null);
        } else {
            List<Long> ghIds = accessService.getAuthorizedGreenhouseIds(UserContext.userId());
            if (ghIds.isEmpty()) {
                return List.of();
            }
            list = greenhouseMapper.selectBatchIds(ghIds);
        }
        // 遍历填充每个大棚下的设备数量
        return list.stream().peek(gh -> {
            LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Device::getGreenhouseId, gh.getId());
            gh.setDeviceCount(deviceMapper.selectCount(wrapper).intValue());
        }).collect(Collectors.toList());
    }

    @Override
    public Greenhouse getById(Long id) {
        return greenhouseMapper.selectById(id);
    }

    @Override
    @Transactional
    public Greenhouse create(Greenhouse gh) {
        greenhouseMapper.insert(gh);
        return gh;
    }

    @Override
    @Transactional
    public Greenhouse update(Long id, Greenhouse gh) {
        gh.setId(id);
        greenhouseMapper.updateById(gh);
        return greenhouseMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return greenhouseMapper.deleteById(id) > 0;
    }

    @Override
    public List<Device> getDevices(Long greenhouseId) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getGreenhouseId, greenhouseId);
        return deviceMapper.selectList(wrapper);
    }
}
