package com.gexianmi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gexianmi.entity.EnvironmentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 按时间桶聚合环境数据（平均值），用于长时间跨度的历史曲线查询，
     * 避免返回过多原始数据导致截断/失真。
     */
    @Select("SELECT device_id, " +
            "FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(recorded_at) / #{bucketSeconds}) * #{bucketSeconds}) AS recorded_at, " +
            "ROUND(AVG(water_temp), 2) AS water_temp, " +
            "ROUND(AVG(ph), 2) AS ph, " +
            "ROUND(AVG(dissolved_oxygen), 2) AS dissolved_oxygen, " +
            "ROUND(AVG(light_intensity), 2) AS light_intensity, " +
            "ROUND(AVG(conductivity), 2) AS conductivity " +
            "FROM environment_records " +
            "WHERE device_id = #{deviceId} AND recorded_at >= #{start} AND recorded_at <= #{end} " +
            "GROUP BY FLOOR(UNIX_TIMESTAMP(recorded_at) / #{bucketSeconds}) " +
            "ORDER BY recorded_at ASC")
    List<EnvironmentRecord> aggregateByBucket(@Param("deviceId") String deviceId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              @Param("bucketSeconds") long bucketSeconds);
}
