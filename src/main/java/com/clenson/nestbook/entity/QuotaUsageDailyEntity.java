package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_quota_usage_daily")
public class QuotaUsageDailyEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private LocalDate usageDate;
    private Integer aiUsedCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

