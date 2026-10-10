package com.gexianmi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gexianmi.entity.Alert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警记录 Mapper 接口
 *
 * <p>对应实体类 {@link Alert} 和数据库表 alerts，
 * 继承 MyBatis-Plus BaseMapper 后自动拥有基础 CRUD 方法</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {
}
