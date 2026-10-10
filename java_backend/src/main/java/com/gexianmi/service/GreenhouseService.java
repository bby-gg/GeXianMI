package com.gexianmi.service;

import com.gexianmi.entity.Device;
import com.gexianmi.entity.Greenhouse;
import java.util.List;

/**
 * 大棚业务接口，定义智慧大棚的增删改查及关联设备查询操作。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
public interface GreenhouseService {

    /**
     * 查询全部大棚列表，包含每个大棚下的设备数量。
     *
     * @return 大棚列表
     */
    List<Greenhouse> listAll();

    /**
     * 根据大棚ID查询大棚详情。
     *
     * @param id 大棚ID
     * @return 大棚实体对象，不存在返回null
     */
    Greenhouse getById(Long id);

    /**
     * 创建新大棚。
     *
     * @param gh 大棚实体对象
     * @return 创建后的大棚对象（含数据库自动生成的ID）
     */
    Greenhouse create(Greenhouse gh);

    /**
     * 根据大棚ID更新大棚信息。
     *
     * @param id 大棚ID
     * @param gh 更新后的大棚实体对象
     * @return 更新后的大棚对象
     */
    Greenhouse update(Long id, Greenhouse gh);

    /**
     * 根据大棚ID删除大棚。
     *
     * @param id 大棚ID
     * @return true表示删除成功，false表示大棚不存在或删除失败
     */
    boolean delete(Long id);

    /**
     * 查询指定大棚下绑定的全部设备列表。
     *
     * @param greenhouseId 大棚ID
     * @return 该大棚关联的设备列表
     */
    List<Device> getDevices(Long greenhouseId);
}
