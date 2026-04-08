package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("push_rule")
public class PushRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal radiusKm;
    private String urgencyLevels;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
