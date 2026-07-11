package com.clenson.nestbook.service;

record WechatXmlMessage(
        String toUserName,
        String fromUserName,
        String createTime,
        String msgType,
        String msgId,
        String content
) {
}
