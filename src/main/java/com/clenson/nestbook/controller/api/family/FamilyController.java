package com.clenson.nestbook.controller.api.family;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mp/family")
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping("/current")
    public ApiResponse<Map<String, Object>> current() {
        return ApiResponse.ok(familyService.currentFamily());
    }

    @GetMapping("/members")
    public ApiResponse<List<Map<String, Object>>> members() {
        return ApiResponse.ok(familyService.members());
    }

    @PatchMapping("/my-nickname")
    public ApiResponse<Map<String, Object>> updateMyNickname(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(familyService.updateMyNickname(request));
    }
}

