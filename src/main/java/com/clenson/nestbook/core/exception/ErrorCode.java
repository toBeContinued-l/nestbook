package com.clenson.nestbook.core.exception;

public enum ErrorCode {
    SUCCESS("0", "success"),
    BAD_REQUEST("400", "请求参数不正确"),
    UNAUTHORIZED("401", "请先登录"),
    FORBIDDEN("403", "无权执行该操作"),
    NOT_FOUND("404", "资源不存在"),
    BINDING_TOKEN_INVALID("BINDING_TOKEN_INVALID", "绑定入口已失效"),
    INVITATION_INVALID("INVITATION_INVALID", "这个邀请已失效"),
    FAMILY_CONFLICT("FAMILY_CONFLICT", "当前版本暂不支持同时加入多个家庭"),
    TRIAL_EXPIRED("TRIAL_EXPIRED", "体验期已结束"),
    AI_QUOTA_EXCEEDED("AI_QUOTA_EXCEEDED", "今天的 AI 解析额度已用完"),
    INTERNAL_ERROR("500", "系统开小差了，请稍后再试");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}

