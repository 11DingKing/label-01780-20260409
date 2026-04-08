package com.help.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.help.mp.entity.HelpRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface HelpRequestMapper extends BaseMapper<HelpRequest> {

    List<HelpRequest> selectNearby(@Param("lat") BigDecimal lat, @Param("lng") BigDecimal lng, @Param("radiusKm") double radiusKm, @Param("status") Integer status, @Param("limit") int limit);
}
