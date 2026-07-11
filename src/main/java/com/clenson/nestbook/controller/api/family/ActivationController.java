package com.clenson.nestbook.controller.api.family;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mp/activation")
public class ActivationController {

    private final FamilyService familyService;

    @PostMapping("/redeem")
    public ApiResponse<Map<String, Object>> redeem(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(familyService.redeemActivation(request));
    }
}

