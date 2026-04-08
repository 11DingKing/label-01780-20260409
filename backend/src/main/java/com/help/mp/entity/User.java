package com.help.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String unionid;
    private String sessionKey;
    private String nickName;
    private String avatarUrl;
    private String phoneEnc;
    private Integer phoneAnon;
    private Integer redFlowerTotal;
    private Integer badgeLevel;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
