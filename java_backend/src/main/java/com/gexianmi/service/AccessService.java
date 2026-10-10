package com.gexianmi.service;

import com.gexianmi.entity.UserGreenhouseAccess;

import java.util.List;

/**
 * 授权管理业务接口。
 *
 * @author GXM-Team
 * @since 2026-07-13
 */
public interface AccessService {

    /**
     * 查询指定用户被授权的大棚ID列表。
     *
     * @param userId 用户ID
     * @return 大棚ID列表
     */
    List<Long> getAuthorizedGreenhouseIds(Long userId);

    /**
     * 查询指定用户的所有授权记录（含详细信息）。
     *
     * @param userId 用户ID
     * @return 授权记录列表
     */
    List<UserGreenhouseAccess> listByUser(Long userId);

    /**
     * 全量覆盖授权：先清空旧记录，再逐条插入新记录（原子操作）。
     *
     * @param userId        用户ID
     * @param greenhouseIds 新授权的大棚ID列表（可为空）
     */
    void saveAccess(Long userId, List<Long> greenhouseIds);

    /**
     * 增量授权：为指定用户追加授权多个大棚（不删已有记录）。
     *
     * @param userId        用户ID
     * @param greenhouseIds 大棚ID列表
     */
    void grant(Long userId, List<Long> greenhouseIds);

    /**
     * 撤销授权：移除指定用户在某个大棚的访问权限。
     *
     * @param userId       用户ID
     * @param greenhouseId 大棚ID
     */
    void revoke(Long userId, Long greenhouseId);
}
