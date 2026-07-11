package com.clenson.nestbook.controller.api.bill;

import com.clenson.nestbook.core.response.ApiResponse;
import com.clenson.nestbook.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mp/bills")
public class BillController {

    private final BillService billService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        return ApiResponse.ok(billService.listBills(filters));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(billService.billDetail(id));
    }
}

