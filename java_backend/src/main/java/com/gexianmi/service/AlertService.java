package com.gexianmi.service;

import com.gexianmi.entity.Alert;
import java.util.List;

/**
 * 告警业务接口，定义告警消息的查询与处理操作。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
public interface AlertService {

    /**
     * 查询指定设备当前处于活跃状态的告警列表。
     *
     * @param deviceId 设备编号，为null时查询所有设备的活跃告警
     * @return 活跃告警列表
     */
    List<Alert> listActive(String deviceId);

    /**
     * 按条件分页查询告警列表，支持按状态和设备过滤。
     *
     * @param status   告警状态，为null时不过滤状态
     * @param deviceId 设备编号，为null时不过滤设备
     * @param limit    返回条数上限，为null或不大于0时不限制
     * @return 符合条件的告警列表
     */
    List<Alert> listAll(String status, String deviceId, Integer limit);

    /**
     * 确认单条告警，将其状态更新为已确认。
     *
     * @param id 告警ID
     * @return true表示确认成功，false表示失败
     */
    boolean confirm(Long id);

    /**
     * 忽略单条告警，将其状态更新为已忽略。
     *
     * @param id 告警ID
     * @return true表示忽略成功，false表示失败
     */
    boolean ignore(Long id);

    /**
     * 批量确认全部活跃告警，将所有活跃状态的告警标记为已确认。
     *
     * @return 确认成功的记录条数
     */
    int confirmAll();
}
