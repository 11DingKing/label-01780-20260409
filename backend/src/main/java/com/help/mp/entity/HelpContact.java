package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("help_contact")
public class HelpContact {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String nameEnc;
    private String phoneEnc;
    private String relation;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
