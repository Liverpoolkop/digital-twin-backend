package com.example.digitaltwin.dto;

import java.util.List;

/**
 * AI对比模式仿真结果
 * 包含AI预测曲线和三种数学拟合曲线，用于前端对比展示
 */
public class AiComparisonResult {
    /** AI预测曲线（0-12h，步长0.5h） */
    private List<TimeValuePoint> aiCurve;

    /** 线性拟合曲线 */
    private List<TimeValuePoint> linearCurve;

    /** 多项式拟合曲线 */
    private List<TimeValuePoint> polynomialCurve;

    /** 对数拟合曲线 */
    private List<TimeValuePoint> logarithmicCurve;

    /** 预测来源标识 */
    private String predictionSource;  // "AI_SUCCESS" | "AI_FAILED"

    /** AI失败时的错误信息 */
    private String errorMessage;

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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
