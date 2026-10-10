package com.gexianmi.service;

import com.gexianmi.entity.Device;
import java.util.List;

/**
 * 设备业务接口，定义物联网设备的注册、查询、更新和删除操作。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
public interface DeviceService {

    /**
     * 查询全部设备列表，包含所属大棚名称信息。
     *
     * @return 设备列表
     */
    List<Device> listAll();

    /**
     * 根据设备编号查询设备详情。
     *
     * @param code 设备唯一编号
     * @return 设备实体对象，不存在返回null
     */
    Device getByCode(String code);

    /**
     * 注册新设备。
     *
     * @param device 设备实体对象
     * @return 创建后的设备对象（含数据库自动生成的字段）
     */
    Device create(Device device);

    /**
     * 根据设备编号更新设备信息。
     *
     * @param code   设备唯一编号
     * @param device 更新后的设备实体对象
     * @return 更新后的设备对象
     */
    Device update(String code, Device device);

    /**
     * 根据设备编号删除设备。
     *
     * @param code 设备唯一编号
     * @return true表示删除成功，false表示设备不存在或删除失败
     */
    boolean delete(String code);
}
