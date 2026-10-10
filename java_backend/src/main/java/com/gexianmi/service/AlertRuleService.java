package com.gexianmi.service;

import com.gexianmi.entity.AlertRule;
import java.util.List;

/**
 * 告警规则业务接口，定义告警规则的查询和更新操作。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
public interface AlertRuleService {

    /**
     * 查询全部告警规则列表。
     *
     * @return 告警规则列表
     */
    List<AlertRule> listAll();

    /**
     * 根据规则ID更新告警规则信息。
     *
     * @param id   告警规则ID
     * @param rule 更新后的告警规则对象
     * @return 更新后的告警规则对象
     */
    AlertRule update(Long id, AlertRule rule);
}
