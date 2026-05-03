package com.example.digitaltwin.dto;

/**
 * 时间-数值点（用于时间序列曲线）
 */
public class TimeValuePoint {
    /** 时间点（小时） */
    private Double time;
    /** 指标数值 */
    private Double value;

    public TimeValuePoint() {
    }

    public TimeValuePoint(Double time, Double value) {
        this.time = time;
        this.value = value;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
