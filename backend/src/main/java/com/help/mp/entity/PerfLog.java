package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("perf_log")
public class PerfLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String uri;
    private String method;
    private Long durationMs;
    private Integer statusCode;
    private LocalDateTime createTime;
}
