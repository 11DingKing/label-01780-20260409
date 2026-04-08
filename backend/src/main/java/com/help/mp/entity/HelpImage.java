package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("help_image")
public class HelpImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long helpId;
    private String fileId;
    private String url;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
