package com.example.massivo.system.dashboard;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboards")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/balances")
    public List<AccountBalanceDTO> balances(@RequestParam UUID companyId) {
        return service.getBalances(companyId);
    }

    @GetMapping("/monthly-summary")
    public List<MonthlySummaryDTO> monthlySummary(@RequestParam UUID companyId, @RequestParam int year) {
        return service.getMonthlySummary(companyId, year);
    }
}
