package com.gexianmi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gexianmi.entity.UserGreenhouseAccess;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-大棚授权 Mapper 接口。
 * <p>
 * 继承 MyBatis-Plus BaseMapper 后自动拥有基础 CRUD 方法。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-07-13
 */
@Mapper
public interface UserGreenhouseAccessMapper extends BaseMapper<UserGreenhouseAccess> {
}
