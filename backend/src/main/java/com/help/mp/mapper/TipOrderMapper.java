package com.help.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.help.mp.entity.TipOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TipOrderMapper extends BaseMapper<TipOrder> {

    @Select("SELECT COALESCE(SUM(amount_cents), 0) FROM tip_order WHERE status = 1")
    Long sumPaidAmount();
}
