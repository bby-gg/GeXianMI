package com.gexianmi.service.impl;

import com.gexianmi.entity.AlertRule;
import com.gexianmi.mapper.AlertRuleMapper;
import com.gexianmi.service.AlertRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 告警规则业务实现，通过MyBatis-Plus操作告警规则表。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Service
@RequiredArgsConstructor
public class AlertRuleServiceImpl implements AlertRuleService {

    /** 告警规则数据访问层 */
    private final AlertRuleMapper mapper;

    @Override
    public List<AlertRule> listAll() {
        return mapper.selectList(null);
    }

    @Override
    @Transactional
    public AlertRule update(Long id, AlertRule rule) {
        rule.setId(id);
        mapper.updateById(rule);
        return mapper.selectById(id);
    }
}
