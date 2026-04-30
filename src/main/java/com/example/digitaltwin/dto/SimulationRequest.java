package com.example.digitaltwin.dto;

/**
 * 仿真接口请求体
 * POST /api/simulation/run
 */
public class SimulationRequest {

    /** 关联实验方案 ID（可选，从实验台账发起仿真时传入） */
    private Long experimentId;

    /** 目标物种：MOUSE / RABBIT / FROG */
    private String animalType;

    /** 目标化学物质 */
    private String chemicalName;

    /** 观测指标名称（与 dataset_raw.indicator_name 一致） */
    private String indicatorName;

    /** 数据筛选：最低环境温度（℃，含） */
    private Double minTemp;

    /** 数据筛选：最高环境温度（℃，含） */
    private Double maxTemp;

    /**
     * 算法模型标识
     * LINEAR       - 简单线性回归
     * POLYNOMIAL   - 二次多项式回归
     * LOGARITHMIC  - 对数回归 y = a + b·ln(x)
     */
    private String algorithmModel;

    /** 目标预测剂量（与 dataset_raw.dosage 单位一致） */
    private Double targetDosage;

    public Long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Long experimentId) {
        this.experimentId = experimentId;
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

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public Double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(Double minTemp) {
        this.minTemp = minTemp;
    }

    public Double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(Double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getAlgorithmModel() {
        return algorithmModel;
    }

    public void setAlgorithmModel(String algorithmModel) {
        this.algorithmModel = algorithmModel;
    }

    public Double getTargetDosage() {
        return targetDosage;
    }

    public void setTargetDosage(Double targetDosage) {
        this.targetDosage = targetDosage;
    }
}
