package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_activation_code")
public class ActivationCodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String codeHash;
    private String status;
    private Long familyId;
    private LocalDateTime usedTime;
    private LocalDateTime createTime;
}

