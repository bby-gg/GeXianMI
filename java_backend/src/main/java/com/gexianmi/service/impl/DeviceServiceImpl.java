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
import java.time.LocalDateTime;
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

    /** 在线判定阈值：最近 N 秒内上报过数据即视为在线 */
    private static final int ONLINE_THRESHOLD_SECONDS = 60;

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
        // 遍历填充每个设备所关联的大棚名称，并根据心跳时间推导在线状态
        return list.stream().peek(d -> {
            if (d.getGreenhouseId() != null) {
                Greenhouse gh = greenhouseMapper.selectById(d.getGreenhouseId());
                d.setGreenhouseName(gh != null ? gh.getName() : null);
            }
            d.setStatus(deriveStatus(d));
        }).collect(Collectors.toList());
    }

    /**
     * 根据心跳时间推导设备在线状态：
     * 维护中保留手动状态，否则最近上报过数据视为在线，反之离线。
     */
    private String deriveStatus(Device d) {
        if ("maintenance".equals(d.getStatus())) {
            return "maintenance";
        }
        if (d.getLastSeen() != null
                && d.getLastSeen().isAfter(LocalDateTime.now().minusSeconds(ONLINE_THRESHOLD_SECONDS))) {
            return "online";
        }
        return "offline";
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
