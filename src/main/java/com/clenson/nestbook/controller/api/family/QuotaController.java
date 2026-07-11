package com.clenson.nestbook.controller.api.family;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mp/quota")
public class QuotaController {

    private final FamilyService familyService;

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        return ApiResponse.ok(familyService.quotaStatus());
    }
}

