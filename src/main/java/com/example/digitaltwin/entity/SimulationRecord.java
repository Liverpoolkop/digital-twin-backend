package com.example.digitaltwin.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户数字孪生仿真预测记录
 * 对应表：simulation_record
 */
public class SimulationRecord {

    /** 主键 */
    private Long id;

    /** 操作人，关联 user.id */
    private Long userId;

    /** 关联实验台账 ID（可选），关联 experiment.id；为 null 表示独立仿真 */
    private Long experimentId;

    /** 目标物种：MOUSE / RABBIT / FROG */
    private String targetAnimal;

    /** 目标化学物质名称 */
    private String targetChemical;

    /** 观测指标名称（与 dataset_raw.indicator_name 一致，用于纵轴标注） */
    private String indicatorName;

    /** 用户输入的预测剂量 */
    private BigDecimal inputDosage;

    /**
     * 所选算法模型
     * LINEAR：简单线性回归
     * POLYNOMIAL：二次多项式回归
     * LOGARITHMIC：对数回归 y = a + b·ln(x)
     */
    private String selectedModel;

    /** 模型输出的预测指标数值 */
    private BigDecimal predictedValue;

    /** 记录创建时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Long experimentId) {
        this.experimentId = experimentId;
    }

    public String getTargetAnimal() {
        return targetAnimal;
    }

    public void setTargetAnimal(String targetAnimal) {
        this.targetAnimal = targetAnimal;
    }

    public String getTargetChemical() {
        return targetChemical;
    }

    public void setTargetChemical(String targetChemical) {
        this.targetChemical = targetChemical;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public BigDecimal getInputDosage() {
        return inputDosage;
    }

    public void setInputDosage(BigDecimal inputDosage) {
        this.inputDosage = inputDosage;
    }

    public String getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
    }

    public BigDecimal getPredictedValue() {
        return predictedValue;
    }

    public void setPredictedValue(BigDecimal predictedValue) {
        this.predictedValue = predictedValue;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
