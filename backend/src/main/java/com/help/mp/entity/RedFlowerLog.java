package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("red_flower_log")
public class RedFlowerLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String bizType;
    private String bizId;
    private Integer amount;
    private Integer balanceAfter;
    private String remark;
    private LocalDateTime createTime;
}
