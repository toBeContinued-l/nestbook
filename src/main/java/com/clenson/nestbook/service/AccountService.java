package com.clenson.nestbook.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccountService {

    public Map<String, Object> login(Map<String, Object> request) {
        return Map.of("status", "TODO", "flow", "mp_code2session_login");
    }

    public Map<String, Object> me() {
        return Map.of("status", "TODO", "flow", "current_mp_user");
    }

    public Map<String, Object> bindingContext(String token) {
        return Map.of("status", "TODO", "flow", "binding_context", "tokenPresent", token != null && !token.isBlank());
    }

    public Map<String, Object> confirmBinding(Map<String, Object> request) {
        return Map.of("status", "TODO", "flow", "confirm_oa_mp_binding");
    }

    public Map<String, Object> bindingGuide() {
        return Map.of("keyword", "绑定", "nextStep", "请先到公众号回复“绑定”，再从公众号返回的小程序入口确认绑定。");
    }
}

