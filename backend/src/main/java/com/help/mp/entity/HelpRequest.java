package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("help_request")
public class HelpRequest {
    @TableField(exist = false)
    private String publisherNickName;
    @TableField(exist = false)
    private String publisherAvatar;
    @TableField(exist = false)
    private Integer publisherBadgeLevel;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private Integer addressAnon;
    private Long contactId;
    private Integer urgencyLevel;
    private String content;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
