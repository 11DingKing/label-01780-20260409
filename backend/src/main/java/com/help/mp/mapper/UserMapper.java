package com.help.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.help.mp.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    int addRedFlower(@Param("userId") Long userId, @Param("amount") int amount);
}
