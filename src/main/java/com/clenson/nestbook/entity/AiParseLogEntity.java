package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_ai_parse_log")
public class AiParseLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private Long userId;
    private String rawText;
    private String promptVersion;
    private String model;
    private String requestJson;
    private String responseJson;
    private String parseStatus;
    private Integer tokenInput;
    private Integer tokenOutput;
    private BigDecimal cost;
    private LocalDateTime createTime;
}

