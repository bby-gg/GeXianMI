package com.gexianmi.service;

import com.gexianmi.dto.EnvironmentDataDTO;
import com.gexianmi.entity.EnvironmentRecord;

import java.util.List;
import java.util.Map;

/**
 * 环境数据业务接口，定义环境监测数据的采集、查询与历史记录操作。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
public interface EnvironmentService {

    /**
     * 保存一条环境监测数据，写入环境记录表。
     *
     * @param dto 环境数据DTO，包含水温、pH、溶解氧、光照强度、电导率等指标
     * @throws RuntimeException 当设备未在系统中注册时抛出
     */
    void saveData(EnvironmentDataDTO dto);

    /**
     * 查询指定设备最新一条环境监测记录。
     *
     * @param deviceId 设备编号
     * @return 最新环境记录，无数据时返回null
     */
    EnvironmentRecord getLatest(String deviceId);

    /**
     * 查询指定设备最近N条环境监测记录，用于绘制趋势曲线图。
     *
     * @param deviceId 设备编号
     * @param limit    返回记录条数上限
     * @return 环境记录列表，按记录时间倒序排列
     */
    List<EnvironmentRecord> getRecent(String deviceId, int limit);

    /**
     * 查询指定设备在时间范围内的历史环境监测记录。
     *
     * @param deviceId 设备编号
     * @param start    开始时间（yyyy-MM-dd HH:mm:ss格式），为null时不限制起始时间
     * @param end      结束时间（yyyy-MM-dd HH:mm:ss格式），为null时不限制结束时间
     * @return 历史环境记录列表，按记录时间正序排列，最多1000条
     */
    List<EnvironmentRecord> getHistory(String deviceId, String start, String end);

    /**
     * 查询指定设备的数据范围信息，包括最早/最晚记录时间和总记录数。
     *
     * @param deviceId 设备编号
     * @return Map包含first（最早记录时间）、last（最晚记录时间）、count（总记录数）
     */
    Map<String, Object> getDataRange(String deviceId);
}
