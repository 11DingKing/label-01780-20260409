package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("help_interaction")
public class HelpInteraction {
    @TableField(exist = false)
    private String helpContent;
    @TableField(exist = false)
    private String fromUserNickName;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long helpId;
    private Long userId;
    private String type;
    private Integer redFlowerAmount;
    private String extra;
    private LocalDateTime createTime;
}
