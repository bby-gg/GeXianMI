package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gexianmi.common.UserContext;
import com.gexianmi.entity.Alert;
import com.gexianmi.mapper.AlertMapper;
import com.gexianmi.service.AccessService;
import com.gexianmi.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警业务实现，提供告警条件查询、确认和忽略等操作。
 * <p>
 * 非管理员用户只能查看被授权大棚下的设备告警。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    /** 告警数据访问层 */
    private final AlertMapper alertMapper;
    /** 授权管理服务 */
    private final AccessService accessService;

    /**
     * 构建带权限过滤的基础查询条件。
     * <p>
     * 管理员：不加额外的大棚过滤条件，可查看全部告警；
     * 操作员/观察者：只查询被授权大棚的告警（通过 alerts.greenhouse_id 字段过滤）。
     * </p>
     *
     * @param status   告警状态，为空则不过滤
     * @param deviceId 设备编号，为空则不过滤
     * @return 带权限过滤的查询条件
     */
    private LambdaQueryWrapper<Alert> buildBaseWrapper(String status, String deviceId) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Alert::getStatus, status);
        }
        if (deviceId != null && !deviceId.isEmpty()) {
            wrapper.eq(Alert::getDeviceId, deviceId);
        }
        // 非管理员：只看授权大棚的告警
        if (!UserContext.isAdmin()) {
            List<Long> ghIds = accessService.getAuthorizedGreenhouseIds(UserContext.userId());
            if (ghIds.isEmpty()) {
                wrapper.apply("1 = -1"); // 无任何授权 → 查不到任何告警
            } else {
                wrapper.in(Alert::getGreenhouseId, ghIds);
            }
        }
        wrapper.orderByDesc(Alert::getTriggeredAt);
        return wrapper;
    }

    @Override
    public List<Alert> listActive(String deviceId) {
        return alertMapper.selectList(buildBaseWrapper("active", deviceId));
    }

    @Override
    public List<Alert> listAll(String status, String deviceId, Integer limit) {
        LambdaQueryWrapper<Alert> wrapper = buildBaseWrapper(status, deviceId);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        return alertMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean confirm(Long id) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setStatus("confirmed");
        alert.setConfirmedAt(LocalDateTime.now());
        return alertMapper.updateById(alert) > 0;
    }

    @Override
    @Transactional
    public boolean ignore(Long id) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setStatus("ignored");
        return alertMapper.updateById(alert) > 0;
    }

    @Override
    @Transactional
    public int confirmAll() {
        LambdaUpdateWrapper<Alert> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Alert::getStatus, "active");
        // 非管理员只能批量确认自己授权大棚的告警
        if (!UserContext.isAdmin()) {
            List<Long> ghIds = accessService.getAuthorizedGreenhouseIds(UserContext.userId());
            if (ghIds.isEmpty()) return 0;
            wrapper.in(Alert::getGreenhouseId, ghIds);
        }
        wrapper.set(Alert::getStatus, "confirmed")
               .set(Alert::getConfirmedAt, LocalDateTime.now());
        return alertMapper.update(null, wrapper);
    }
}
