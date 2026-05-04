package com.example.digitaltwin.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生理指标基准实体
 * 用于存储不同物种各器官指标的正常范围和危险阈值
 */
public class IndicatorBaseline {

    /** 主键 */
    private Long id;

    /** 物种（MOUSE/RABBIT/FROG） */
    private String species;

    /** 器官名称（Heart/Liver/Lung） */
    private String organName;

    /** 指标名称 */
    private String indicatorName;

    /** 正常值下限 */
    private BigDecimal normalMin;

    /** 正常值上限 */
    private BigDecimal normalMax;

    /** 危险阈值 */
    private BigDecimal dangerThreshold;

    /** 单位 */
    private String unit;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getOrganName() {
        return organName;
    }

    public void setOrganName(String organName) {
        this.organName = organName;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public BigDecimal getNormalMin() {
        return normalMin;
    }

    public void setNormalMin(BigDecimal normalMin) {
        this.normalMin = normalMin;
    }

    public BigDecimal getNormalMax() {
        return normalMax;
    }

    public void setNormalMax(BigDecimal normalMax) {
        this.normalMax = normalMax;
    }

    public BigDecimal getDangerThreshold() {
        return dangerThreshold;
    }

    public void setDangerThreshold(BigDecimal dangerThreshold) {
        this.dangerThreshold = dangerThreshold;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
