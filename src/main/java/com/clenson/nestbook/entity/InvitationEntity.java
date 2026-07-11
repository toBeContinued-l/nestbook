package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_invitation")
public class InvitationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private Long inviterUserId;
    private String inviteCodeHash;
    private String status;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}

