package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_bill")
public class BillEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private Long userId;
    private String oaOpenid;
    private Integer billType;
    private BigDecimal amount;
    private String category;
    private LocalDate billDate;
    private String remark;
    private String rawText;
    private String wechatMsgId;
    private String source;
    private String parseModel;
    private BigDecimal confidence;
    private LocalDateTime createTime;
    @TableField("is_deleted")
    private Boolean deleted;
    private LocalDateTime deletedTime;
    private Long deletedByUserId;
}
