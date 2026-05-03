package com.example.digitaltwin.dto;

/**
 * 器官指标预测结果
 */
public class OrganIndicatorResult {

    /** 器官名称 */
    private String organ;

    /** 指标名称 */
    private String indicatorName;

    /** 预测值 */
    private Double predictedValue;

    /** 单位 */
    private String unit;

    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public Double getPredictedValue() {
        return predictedValue;
    }

    public void setPredictedValue(Double predictedValue) {
        this.predictedValue = predictedValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
