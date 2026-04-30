package com.example.digitaltwin.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 历史原始实验数据（算法训练语料）
 * 对应表：dataset_raw
 */
public class DatasetRaw {

    /** 主键 */
    private Long id;

    /** 实验物种：MOUSE / RABBIT / FROG */
    private String animalType;

    /** 化学物质名称 */
    private String chemicalName;

    /** 给药剂量（如 mg/kg） */
    private BigDecimal dosage;

    /** 生理/毒性指标名称（如 ALT、皮肤反应指数） */
    private String indicatorName;

    /** 该指标的真实观测数值 */
    private BigDecimal indicatorValue;

    /** 实验环境温度（℃），用于筛选数据范围 */
    private BigDecimal temperature;

    /** 样本入库时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public String getChemicalName() {
        return chemicalName;
    }

    public void setChemicalName(String chemicalName) {
        this.chemicalName = chemicalName;
    }

    public BigDecimal getDosage() {
        return dosage;
    }

    public void setDosage(BigDecimal dosage) {
        this.dosage = dosage;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public BigDecimal getIndicatorValue() {
        return indicatorValue;
    }

    public void setIndicatorValue(BigDecimal indicatorValue) {
        this.indicatorValue = indicatorValue;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
