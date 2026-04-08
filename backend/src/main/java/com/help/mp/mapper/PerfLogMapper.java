package com.help.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.help.mp.entity.PerfLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface PerfLogMapper extends BaseMapper<PerfLog> {

    @Select("SELECT " +
            "COUNT(*) AS total, " +
            "ROUND(AVG(duration_ms), 2) AS avgMs, " +
            "ROUND(MAX(duration_ms), 2) AS maxMs, " +
            "COUNT(CASE WHEN duration_ms > 500 THEN 1 END) AS slowCount " +
            "FROM perf_log WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR)")
    Map<String, Object> getStats(@Param("hours") int hours);

    @Select("SELECT duration_ms FROM perf_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "ORDER BY duration_ms ASC")
    java.util.List<Long> getAllDurations(@Param("hours") int hours);
}
