package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_location")
public class UserLocation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime updateTime;
}
