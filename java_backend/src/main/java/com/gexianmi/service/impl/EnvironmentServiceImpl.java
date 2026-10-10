package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gexianmi.common.ForbiddenException;
import com.gexianmi.common.UserContext;
import com.gexianmi.dto.EnvironmentDataDTO;
import com.gexianmi.entity.Alert;
import com.gexianmi.entity.AlertRule;
import com.gexianmi.entity.Device;
import com.gexianmi.entity.EnvironmentRecord;
import com.gexianmi.mapper.AlertMapper;
import com.gexianmi.mapper.AlertRuleMapper;
import com.gexianmi.mapper.DeviceMapper;
import com.gexianmi.mapper.EnvironmentRecordMapper;
import com.gexianmi.service.AccessService;
import com.gexianmi.service.AlertSseService;
import com.gexianmi.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
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
    /** 告警数据访问层 */
    private final AlertMapper alertMapper;
    /** 告警规则数据访问层 */
    private final AlertRuleMapper alertRuleMapper;
    /** 告警实时推送服务（SSE） */
    private final AlertSseService alertSseService;

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

        // 更新设备心跳时间（最近一次上报），用于在线/离线状态判断
        device.setLastSeen(LocalDateTime.now());
        deviceMapper.updateById(device);

        EnvironmentRecord record = new EnvironmentRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setRecordedAt(LocalDateTime.parse(dto.getTimestamp(), FORMATTER));
        record.setWaterTemp(dto.getWaterTemp());
        record.setPh(dto.getPh());
        record.setDissolvedOxygen(dto.getDissolvedOxygen());
        record.setLightIntensity(dto.getLightIntensity());
        record.setConductivity(dto.getConductivity());

        mapper.insert(record);

        // 落库后按告警规则逐参数比对，越界则自动生成告警
        generateAlerts(dto, device);
    }

    /**
     * 依据告警规则对本次上报数据做阈值判断，越界则生成告警记录。
     * <p>
     * 同一设备同一参数若已存在活跃告警则跳过，避免持续越界时重复刷屏。
     * </p>
     */
    private void generateAlerts(EnvironmentDataDTO dto, Device device) {
        LambdaQueryWrapper<AlertRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(AlertRule::getEnabled, 1);
        List<AlertRule> rules = alertRuleMapper.selectList(ruleWrapper);

        for (AlertRule rule : rules) {
            BigDecimal value = getParamValue(rule.getParam(), dto);
            if (value == null) continue;

            boolean lowBreach = rule.getThresholdLow() != null
                    && value.compareTo(rule.getThresholdLow()) < 0;
            boolean highBreach = rule.getThresholdHigh() != null
                    && value.compareTo(rule.getThresholdHigh()) > 0;

            if (lowBreach || highBreach) {
                // 已有活跃告警则跳过，避免每 5 秒重复刷一条
                LambdaQueryWrapper<Alert> dupWrapper = new LambdaQueryWrapper<>();
                dupWrapper.eq(Alert::getDeviceId, dto.getDeviceId())
                          .eq(Alert::getParam, rule.getParam())
                          .eq(Alert::getStatus, "active");
                if (alertMapper.selectCount(dupWrapper) > 0) continue;

                Alert alert = new Alert();
                alert.setDeviceId(dto.getDeviceId());
                alert.setGreenhouseId(device.getGreenhouseId());
                alert.setParam(rule.getParam());
                alert.setParamName(rule.getParamName());
                alert.setValue(value);
                alert.setThresholdLow(rule.getThresholdLow());
                alert.setThresholdHigh(rule.getThresholdHigh());
                alert.setLevel(rule.getLevel());
                alert.setMessage(buildAlertMessage(rule, value, lowBreach));
                alert.setStatus("active");
                alert.setTriggeredAt(LocalDateTime.now());
                alertMapper.insert(alert);
                // 实时推送新告警给已订阅的前端
                alertSseService.broadcast("new-alert", alert);
            } else {
                // 值回到正常区间，自动关闭该设备该参数的活跃告警
                resolveActiveAlerts(dto.getDeviceId(), rule.getParam());
            }
        }
    }

    /**
     * 值恢复正常时，自动将该设备该参数的活跃告警置为「已恢复」。
     */
    private void resolveActiveAlerts(String deviceId, String param) {
        LambdaUpdateWrapper<Alert> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Alert::getDeviceId, deviceId)
               .eq(Alert::getParam, param)
               .eq(Alert::getStatus, "active")
               .set(Alert::getStatus, "resolved");
        alertMapper.update(null, wrapper);
    }

    /** 根据参数标识从上报数据中取出对应的指标值 */
    private BigDecimal getParamValue(String param, EnvironmentDataDTO dto) {
        return switch (param) {
            case "waterTemp" -> dto.getWaterTemp();
            case "ph" -> dto.getPh();
            case "dissolvedOxygen" -> dto.getDissolvedOxygen();
            case "lightIntensity" -> dto.getLightIntensity();
            case "conductivity" -> dto.getConductivity();
            default -> null;
        };
    }

    /** 生成告警描述，格式与已有告警保持一致：xx偏低/偏高: 值+单位，阈值: 阈值+单位 */
    private String buildAlertMessage(AlertRule rule, BigDecimal value, boolean lowBreach) {
        String unit = rule.getUnit() != null ? rule.getUnit() : "";
        BigDecimal threshold = lowBreach ? rule.getThresholdLow() : rule.getThresholdHigh();
        String direction = lowBreach ? "偏低" : "偏高";
        return rule.getParamName() + direction + ": " + value + unit + "，阈值: " + threshold + unit;
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
            throw new ForbiddenException("无权访问设备 " + deviceId + " 的数据，该设备不在您的授权范围内");
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
        LocalDateTime startTime = (start != null && !start.isEmpty()) ? parseTime(start) : null;
        LocalDateTime endTime = (end != null && !end.isEmpty()) ? parseTime(end) : null;

        // 时间跨度较大时按时间桶聚合，避免返回过多原始数据导致截断/失真
        if (startTime != null && endTime != null) {
            long spanSeconds = Duration.between(startTime, endTime).getSeconds();
            if (spanSeconds > 7200) {
                return mapper.aggregateByBucket(deviceId, startTime, endTime, pickBucketSeconds(spanSeconds));
            }
        }

        LambdaQueryWrapper<EnvironmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentRecord::getDeviceId, deviceId);
        if (startTime != null) {
            wrapper.ge(EnvironmentRecord::getRecordedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(EnvironmentRecord::getRecordedAt, endTime);
        }
        wrapper.orderByAsc(EnvironmentRecord::getRecordedAt);
        // 小范围直接返回原始数据，上限 1000 条
        wrapper.last("LIMIT 1000");
        return mapper.selectList(wrapper);
    }

    /** 根据时间跨度选择一个合适的时间桶大小（秒），使聚合后点数约在几百到上千 */
    private long pickBucketSeconds(long spanSeconds) {
        long target = Math.max(30, spanSeconds / 800);
        long[] candidates = {30, 60, 300, 600, 1800, 3600, 7200, 21600, 43200, 86400};
        for (long c : candidates) {
            if (c >= target) return c;
        }
        return 86400;
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
