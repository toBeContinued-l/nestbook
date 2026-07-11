package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_family")
public class FamilyEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long creatorUserId;
    private LocalDateTime createTime;
    private LocalDateTime trialEndTime;
    @TableField("is_activated")
    private Boolean activated;
    private LocalDateTime activatedTime;
    private Integer dailyAiLimit;
    private String status;
}
