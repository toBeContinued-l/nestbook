package com.clenson.nestbook.controller.api.bill;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mp/reports")
public class ReportController {

    private final BillService billService;

    @GetMapping("/{yyyyMM}")
    public ApiResponse<Map<String, Object>> report(@PathVariable String yyyyMM) {
        return ApiResponse.ok(billService.report(yyyyMM));
    }

    @GetMapping("/latest")
    public ApiResponse<Map<String, Object>> latest() {
        return ApiResponse.ok(billService.latestReport());
    }
}

