package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("push_log")
public class PushLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long helpId;
    private Long ruleId;
    /** 本次推送总人数 */
    private Integer totalCount;
    /** 成功发送数 */
    private Integer successCount;
    /** 失败数 */
    private Integer failCount;
    /** 到达率 = successCount / totalCount */
    private Double reachRate;
    private LocalDateTime createTime;
}
