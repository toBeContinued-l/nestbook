package com.clenson.nestbook.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FamilyService {

    public Map<String, Object> currentFamily() {
        return Map.of("status", "TODO", "flow", "current_family");
    }

    public List<Map<String, Object>> members() {
        return List.of();
    }

    public Map<String, Object> updateMyNickname(Map<String, Object> request) {
        return Map.of("status", "TODO", "flow", "update_my_family_nickname");
    }

    public Map<String, Object> createInvitation() {
        return Map.of("status", "TODO", "flow", "create_invitation");
    }

    public Map<String, Object> invitationContext(String code) {
        return Map.of("status", "TODO", "flow", "invitation_context", "codePresent", code != null && !code.isBlank());
    }

    public Map<String, Object> joinInvitation(String code) {
        return Map.of("status", "TODO", "flow", "join_invitation", "codePresent", code != null && !code.isBlank());
    }

    public Map<String, Object> quotaStatus() {
        return Map.of("status", "TODO", "flow", "daily_ai_quota_status");
    }

    public Map<String, Object> redeemActivation(Map<String, Object> request) {
        return Map.of("status", "TODO", "flow", "redeem_activation_code");
    }
}

