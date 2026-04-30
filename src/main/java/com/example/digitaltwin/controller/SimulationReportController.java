package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.SimulationRecord;
import com.example.digitaltwin.service.SimulationReportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class SimulationReportController {

    private final SimulationReportService simulationReportService;

    public SimulationReportController(SimulationReportService simulationReportService) {
        this.simulationReportService = simulationReportService;
    }

    @GetMapping("/me")
    public Result<PageResult<SimulationRecord>> myReports(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        return Result.success(simulationReportService.listCurrentUserRecords(pageNum, pageSize));
    }
}
