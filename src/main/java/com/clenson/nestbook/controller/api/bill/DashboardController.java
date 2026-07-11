package com.clenson.nestbook.controller.api.bill;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mp/dashboard")
public class DashboardController {

    private final BillService billService;

    @GetMapping("/monthly")
    public ApiResponse<Map<String, Object>> monthly() {
        return ApiResponse.ok(billService.monthlyDashboard());
    }
}

