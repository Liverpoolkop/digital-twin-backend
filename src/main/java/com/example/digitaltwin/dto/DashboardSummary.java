package com.example.digitaltwin.dto;

import java.math.BigDecimal;

public class DashboardSummary {

    private long totalUsers;
    private long totalSimulations;
    private long weeklySimulations;
    private long pendingExperimentApprovals;
    private long approvedExperiments;
    private BigDecimal replacementRate;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalSimulations() {
        return totalSimulations;
    }

    public void setTotalSimulations(long totalSimulations) {
        this.totalSimulations = totalSimulations;
    }

    public long getWeeklySimulations() {
        return weeklySimulations;
    }

    public void setWeeklySimulations(long weeklySimulations) {
        this.weeklySimulations = weeklySimulations;
    }

    public long getPendingExperimentApprovals() {
        return pendingExperimentApprovals;
    }

    public void setPendingExperimentApprovals(long pendingExperimentApprovals) {
        this.pendingExperimentApprovals = pendingExperimentApprovals;
    }

    public long getApprovedExperiments() {
        return approvedExperiments;
    }

    public void setApprovedExperiments(long approvedExperiments) {
        this.approvedExperiments = approvedExperiments;
    }

    public BigDecimal getReplacementRate() {
        return replacementRate;
    }

    public void setReplacementRate(BigDecimal replacementRate) {
        this.replacementRate = replacementRate;
    }
}
