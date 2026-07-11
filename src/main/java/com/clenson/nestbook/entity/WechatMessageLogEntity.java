package com.clenson.nestbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_wechat_message_log")
public class WechatMessageLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String oaOpenid;
    private String wechatMsgId;
    private String rawText;
    private String messageType;
    private String handleType;
    private String handleStatus;
    private String errorMessage;
    private LocalDateTime createTime;
}

