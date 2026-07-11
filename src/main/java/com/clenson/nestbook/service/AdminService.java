package com.clenson.nestbook.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    public Map<String, Object> createActivationCodes(Map<String, Object> request) {
        return Map.of("status", "TODO", "flow", "create_activation_codes");
    }

    public Map<String, Object> familyDetail(Long id) {
        return Map.of("status", "TODO", "flow", "admin_family_detail", "id", id);
    }

    public List<Map<String, Object>> aiLogs(Map<String, Object> filters) {
        return List.of();
    }
}

