package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.DashboardSummary;
import com.example.digitaltwin.service.DashboardService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public Result<DashboardSummary> summary() {
        return Result.success(dashboardService.getSummary());
    }
}
