package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("badge")
public class Badge {
    @TableId
    private Integer level;
    private String name;
    private String iconUrl;
    private Integer minFlowers;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
