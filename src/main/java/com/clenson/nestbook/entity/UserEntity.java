package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String oaOpenid;
    private String mpOpenid;
    private String nickname;
    private String role;
    private String status;
    private LocalDateTime createTime;
}
