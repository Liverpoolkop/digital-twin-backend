package com.example.digitaltwin.dto;

import java.util.List;
import java.util.Map;

/**
 * 多指标AI对比结果汇总
 */
public class MultiOrganAiComparisonResult {
    private List<IndicatorAiComparison> indicators;
    private String predictionSource;
    private Map<String, Double> organDamageScores;

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

    public Map<String, Double> getOrganDamageScores() {
        return organDamageScores;
    }

    public void setOrganDamageScores(Map<String, Double> organDamageScores) {
        this.organDamageScores = organDamageScores;
    }
}
