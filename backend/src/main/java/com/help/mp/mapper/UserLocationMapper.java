package com.help.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.help.mp.entity.UserLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserLocationMapper extends BaseMapper<UserLocation> {

    /**
     * 查询指定半径内的粉丝openid（Haversine公式）
     */
    @Select("SELECT openid FROM user_location " +
            "WHERE latitude IS NOT NULL AND longitude IS NOT NULL " +
            "AND (6371 * ACOS(COS(RADIANS(#{lat})) * COS(RADIANS(latitude)) * COS(RADIANS(longitude) - RADIANS(#{lng})) " +
            "+ SIN(RADIANS(#{lat})) * SIN(RADIANS(latitude)))) <= #{radiusKm}")
    List<String> selectOpenidsWithinRadius(@Param("lat") BigDecimal lat, @Param("lng") BigDecimal lng,
                                           @Param("radiusKm") double radiusKm);
}
