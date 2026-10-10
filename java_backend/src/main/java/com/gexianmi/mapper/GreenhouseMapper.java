package com.gexianmi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gexianmi.entity.Greenhouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 大棚/养殖基地 Mapper 接口
 *
 * <p>对应实体类 {@link Greenhouse} 和数据库表 greenhouses，
 * 继承 MyBatis-Plus BaseMapper 后自动拥有基础 CRUD 方法</p>
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Mapper
public interface GreenhouseMapper extends BaseMapper<Greenhouse> {
}
