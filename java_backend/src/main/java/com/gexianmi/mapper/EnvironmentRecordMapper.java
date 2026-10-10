package com.gexianmi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gexianmi.entity.EnvironmentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 环境数据记录 Mapper 接口
 *
 * <p>对应实体类 {@link EnvironmentRecord} 和数据库表 environment_records，
 * 继承 MyBatis-Plus BaseMapper 后自动拥有基础 CRUD 方法</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Mapper
public interface EnvironmentRecordMapper extends BaseMapper<EnvironmentRecord> {
}
