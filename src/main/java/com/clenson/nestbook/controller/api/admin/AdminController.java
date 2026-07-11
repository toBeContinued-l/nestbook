package com.clenson.nestbook.controller.api.admin;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/activation-codes")
    public ApiResponse<Map<String, Object>> createActivationCodes(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(adminService.createActivationCodes(request));
    }

    @GetMapping("/families/{id}")
    public ApiResponse<Map<String, Object>> familyDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminService.familyDetail(id));
    }

    @GetMapping("/ai-logs")
    public ApiResponse<List<Map<String, Object>>> aiLogs(@RequestParam Map<String, Object> filters) {
        return ApiResponse.ok(adminService.aiLogs(filters));
    }
}

