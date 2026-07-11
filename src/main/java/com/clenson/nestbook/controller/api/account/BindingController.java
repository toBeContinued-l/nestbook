package com.clenson.nestbook.controller.api.account;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mp/bind")
public class BindingController {

    private final AccountService accountService;

    @GetMapping("/context")
    public ApiResponse<Map<String, Object>> context(@RequestParam String token) {
        return ApiResponse.ok(accountService.bindingContext(token));
    }

    @PostMapping("/confirm")
    public ApiResponse<Map<String, Object>> confirm(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(accountService.confirmBinding(request));
    }

    @GetMapping("/guide")
    public ApiResponse<Map<String, Object>> guide() {
        return ApiResponse.ok(accountService.bindingGuide());
    }
}

