package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.dto.DashboardSummary;
import com.example.digitaltwin.mapper.SimulationRecordMapper;
import com.example.digitaltwin.service.DashboardService;
import com.example.digitaltwin.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final UserService userService;
    private final SimulationRecordMapper simulationRecordMapper;

    public DashboardServiceImpl(UserService userService, SimulationRecordMapper simulationRecordMapper) {
        this.userService = userService;
        this.simulationRecordMapper = simulationRecordMapper;
    }

    @Override
    public DashboardSummary getSummary() {
        long totalUsers = userService.countUsers();
        long totalSimulations = simulationRecordMapper.countAll();
        long weeklySimulations = simulationRecordMapper.countCreatedSince(7);
        long pendingApprovals = simulationRecordMapper.countPendingExperiments();
        long approvedExperiments = simulationRecordMapper.countApprovedExperiments();

        DashboardSummary summary = new DashboardSummary();
        summary.setTotalUsers(totalUsers);
        summary.setTotalSimulations(totalSimulations);
        summary.setWeeklySimulations(weeklySimulations);
        summary.setPendingExperimentApprovals(pendingApprovals);
        summary.setApprovedExperiments(approvedExperiments);
        summary.setReplacementRate(calculateReplacementRate(totalSimulations, approvedExperiments));
        return summary;
    }

    private BigDecimal calculateReplacementRate(long totalSimulations, long approvedExperiments) {
        if (approvedExperiments <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(totalSimulations)
                .multiply(HUNDRED)
                .divide(BigDecimal.valueOf(approvedExperiments), 2, RoundingMode.HALF_UP);
    }
}
