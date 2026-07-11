package com.clenson.nestbook.controller.api.family;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mp/family/invitations")
public class InvitationController {

    private final FamilyService familyService;

    @PostMapping
    public ApiResponse<Map<String, Object>> create() {
        return ApiResponse.ok(familyService.createInvitation());
    }

    @GetMapping("/{code}")
    public ApiResponse<Map<String, Object>> context(@PathVariable String code) {
        return ApiResponse.ok(familyService.invitationContext(code));
    }

    @PostMapping("/{code}/join")
    public ApiResponse<Map<String, Object>> join(@PathVariable String code) {
        return ApiResponse.ok(familyService.joinInvitation(code));
    }
}

