package com.example.digitaltwin.dto;

import java.util.List;

/**
 * 单个指标的AI对比结果
 */
public class IndicatorAiComparison {
    private String indicatorName;
    private List<TimeValuePoint> aiCurve;
    private List<TimeValuePoint> linearCurve;
    private List<TimeValuePoint> polynomialCurve;
    private List<TimeValuePoint> logarithmicCurve;
    private String predictionSource;

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public List<TimeValuePoint> getAiCurve() {
        return aiCurve;
    }

    public void setAiCurve(List<TimeValuePoint> aiCurve) {
        this.aiCurve = aiCurve;
    }

    public List<TimeValuePoint> getLinearCurve() {
        return linearCurve;
    }

    public void setLinearCurve(List<TimeValuePoint> linearCurve) {
        this.linearCurve = linearCurve;
    }

    public List<TimeValuePoint> getPolynomialCurve() {
        return polynomialCurve;
    }

    public void setPolynomialCurve(List<TimeValuePoint> polynomialCurve) {
        this.polynomialCurve = polynomialCurve;
    }

    public List<TimeValuePoint> getLogarithmicCurve() {
        return logarithmicCurve;
    }

    public void setLogarithmicCurve(List<TimeValuePoint> logarithmicCurve) {
        this.logarithmicCurve = logarithmicCurve;
    }

    public String getPredictionSource() {
        return predictionSource;
    }

    public void setPredictionSource(String predictionSource) {
        this.predictionSource = predictionSource;
    }
}
