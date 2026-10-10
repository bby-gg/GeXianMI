package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexianmi.common.UserContext;
import com.gexianmi.entity.Device;
import com.gexianmi.entity.Greenhouse;
import com.gexianmi.mapper.DeviceMapper;
import com.gexianmi.mapper.GreenhouseMapper;
import com.gexianmi.service.AccessService;
import com.gexianmi.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备业务实现，提供设备的增删改查及大棚名称填充功能。
 * <p>
 * 非管理员用户仅能看到被授权大棚下的设备。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    /** 设备数据访问层 */
    private final DeviceMapper deviceMapper;
    /** 大棚数据访问层 */
    private final GreenhouseMapper greenhouseMapper;
    /** 授权管理服务 */
    private final AccessService accessService;

    @Override
    public List<Device> listAll() {
        List<Device> list;
        // 管理员：查全部；操作员/观察者：只查授权大棚下的设备
        if (UserContext.isAdmin()) {
            list = deviceMapper.selectList(null);
        } else {
            List<Long> ghIds = accessService.getAuthorizedGreenhouseIds(UserContext.userId());
            if (ghIds.isEmpty()) {
                return List.of();
            }
            LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Device::getGreenhouseId, ghIds);
            list = deviceMapper.selectList(wrapper);
        }
        // 遍历填充每个设备所关联的大棚名称
        return list.stream().peek(d -> {
            if (d.getGreenhouseId() != null) {
                Greenhouse gh = greenhouseMapper.selectById(d.getGreenhouseId());
                d.setGreenhouseName(gh != null ? gh.getName() : null);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Device getByCode(String code) {
        return deviceMapper.selectById(code);
    }

    @Override
    @Transactional
    public Device create(Device device) {
        deviceMapper.insert(device);
        return device;
    }

    @Override
    @Transactional
    public Device update(String code, Device device) {
        device.setCode(code);
        deviceMapper.updateById(device);
        return deviceMapper.selectById(code);
    }

    @Override
    @Transactional
    public boolean delete(String code) {
        return deviceMapper.deleteById(code) > 0;
    }
}
