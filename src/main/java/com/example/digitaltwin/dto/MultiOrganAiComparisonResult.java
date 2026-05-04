package com.example.digitaltwin.dto;

import java.util.List;

/**
 * 多指标AI对比结果汇总
 */
public class MultiOrganAiComparisonResult {
    private List<IndicatorAiComparison> indicators;
    private String predictionSource;

    public List<IndicatorAiComparison> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<IndicatorAiComparison> indicators) {
        this.indicators = indicators;
    }

    public String getPredictionSource() {
        return predictionSource;
    }

    public void setPredictionSource(String predictionSource) {
        this.predictionSource = predictionSource;
    }
}
