package com.clenson.nestbook.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BillService {

    public List<Map<String, Object>> listBills(Map<String, Object> filters) {
        return List.of();
    }

    public Map<String, Object> billDetail(Long id) {
        return Map.of("status", "TODO", "flow", "bill_detail", "id", id);
    }

    public Map<String, Object> monthlyDashboard() {
        return Map.of("status", "TODO", "flow", "monthly_dashboard");
    }

    public Map<String, Object> report(String yyyyMM) {
        return Map.of("status", "TODO", "flow", "monthly_report", "yyyyMM", yyyyMM);
    }

    public Map<String, Object> latestReport() {
        return Map.of("status", "TODO", "flow", "latest_monthly_report");
    }
}

