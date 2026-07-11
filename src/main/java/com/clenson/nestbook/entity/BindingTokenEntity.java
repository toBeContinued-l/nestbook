package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_binding_token")
public class BindingTokenEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tokenHash;
    private String oaOpenid;
    private Long oaUserId;
    private Long familyId;
    private String status;
    private LocalDateTime expireTime;
    private LocalDateTime usedTime;
    private LocalDateTime createTime;
}

