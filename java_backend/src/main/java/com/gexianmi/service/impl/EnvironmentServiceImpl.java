package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexianmi.common.UserContext;
import com.gexianmi.dto.EnvironmentDataDTO;
import com.gexianmi.entity.Device;
import com.gexianmi.entity.EnvironmentRecord;
import com.gexianmi.mapper.DeviceMapper;
import com.gexianmi.mapper.EnvironmentRecordMapper;
import com.gexianmi.service.AccessService;
import com.gexianmi.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环境数据业务实现，处理环境监测数据的存储、查询和历史数据范围统计。
 * <p>
 * 非管理员用户只能访问被授权大棚下设备的数据，
 * 访问未授权设备的数据时将返回 403 错误。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {

    /** 环境记录数据访问层 */
    private final EnvironmentRecordMapper mapper;
    /** 设备数据访问层 */
    private final DeviceMapper deviceMapper;
    /** 授权管理服务 */
    private final AccessService accessService;

    /** 前端传来时间字符串的标准格式 */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public void saveData(EnvironmentDataDTO dto) {
        // 校验设备是否已在系统中注册，未注册设备的数据直接拒绝
        Device device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new RuntimeException("设备未注册: " + dto.getDeviceId() + "，请先在设备管理页注册");
        }

        EnvironmentRecord record = new EnvironmentRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setRecordedAt(LocalDateTime.parse(dto.getTimestamp(), FORMATTER));
        record.setWaterTemp(dto.getWaterTemp());
        record.setPh(dto.getPh());
        record.setDissolvedOxygen(dto.getDissolvedOxygen());
        record.setLightIntensity(dto.getLightIntensity());
        record.setConductivity(dto.getConductivity());

        mapper.insert(record);
    }

    /**
     * 校验当前用户是否有权访问指定设备的数据。
     * <p>
     * 管理员不受限；操作员/观察者只能查看被授权大棚下的设备。
     * 设备无归属（greenhouseId 为 null）时放行，由业务层自行处理。
     * </p>
     */
    private void checkDeviceAccess(String deviceId) {
        if (UserContext.isAdmin()) return;
        Device device = deviceMapper.selectById(deviceId);
        if (device == null || device.getGreenhouseId() == null) return;
        List<Long> ghIds = accessService.getAuthorizedGreenhouseIds(UserContext.userId());
        if (!ghIds.contains(device.getGreenhouseId())) {
            throw new RuntimeException("无权访问设备 " + deviceId + " 的数据，该设备不在您的授权范围内");
        }
    }

    @Override
    public EnvironmentRecord getLatest(String deviceId) {
        checkDeviceAccess(deviceId);
        LambdaQueryWrapper<EnvironmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentRecord::getDeviceId, deviceId)
               .orderByDesc(EnvironmentRecord::getRecordedAt)
               .last("LIMIT 1");
        return mapper.selectOne(wrapper);
    }

    @Override
    public List<EnvironmentRecord> getRecent(String deviceId, int limit) {
        checkDeviceAccess(deviceId);
        LambdaQueryWrapper<EnvironmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentRecord::getDeviceId, deviceId)
               .orderByDesc(EnvironmentRecord::getRecordedAt)
               .last("LIMIT " + limit);
        return mapper.selectList(wrapper);
    }

    @Override
    public List<EnvironmentRecord> getHistory(String deviceId, String start, String end) {
        checkDeviceAccess(deviceId);
        LambdaQueryWrapper<EnvironmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentRecord::getDeviceId, deviceId);
        if (start != null && !start.isEmpty()) {
            wrapper.ge(EnvironmentRecord::getRecordedAt, parseTime(start));
        }
        if (end != null && !end.isEmpty()) {
            wrapper.le(EnvironmentRecord::getRecordedAt, parseTime(end));
        }
        wrapper.orderByAsc(EnvironmentRecord::getRecordedAt);
        // 历史查询上限1000条，防止数据量过大
        wrapper.last("LIMIT 1000");
        return mapper.selectList(wrapper);
    }

    /**
     * 解析时间字符串，兼容多种常见日期时间格式。
     *
     * @param s 时间字符串
     * @return 解析后的LocalDateTime，字符串为空返回null
     */
    private LocalDateTime parseTime(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e2) {
                // 回退尝试仅日期格式
                try {
                    return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e3) {
                    return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
            }
        }
    }

    @Override
    public Map<String, Object> getDataRange(String deviceId) {
        checkDeviceAccess(deviceId);
        List<EnvironmentRecord> all = getRecent(deviceId, 1);
        if (all == null || all.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("first", null);
            empty.put("last", null);
            empty.put("count", 0);
            return empty;
        }
        // 最新一条记录
        EnvironmentRecord last = all.get(0);

        // 查询最早一条记录
        LambdaQueryWrapper<EnvironmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentRecord::getDeviceId, deviceId)
               .orderByAsc(EnvironmentRecord::getRecordedAt)
               .last("LIMIT 1");
        EnvironmentRecord first = mapper.selectOne(wrapper);

        // 统计该设备的总记录条数
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<EnvironmentRecord>()
                        .eq(EnvironmentRecord::getDeviceId, deviceId));

        Map<String, Object> range = new HashMap<>();
        range.put("first", first != null ? first.getRecordedAt().toString() : null);
        range.put("last", last.getRecordedAt().toString());
        range.put("count", count);
        return range;
    }
}
