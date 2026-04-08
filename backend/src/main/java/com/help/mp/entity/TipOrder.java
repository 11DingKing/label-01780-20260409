package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tip_order")
public class TipOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long helpId;
    private Long userId;
    private Integer amountCents;
    private String wxTransactionId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime updateTime;
}
