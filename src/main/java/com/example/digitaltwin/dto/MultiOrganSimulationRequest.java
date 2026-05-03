package com.example.digitaltwin.dto;

import java.util.List;

/**
 * 多器官协同仿真请求
 */
public class MultiOrganSimulationRequest {

    /** 关联实验ID（可选） */
    private Long experimentId;

    /** 目标动物种类 */
    private String targetAnimal;

    /** 目标化学物质 */
    private String targetChemical;

    /** 器官列表：Heart/Liver/Lung */
    private List<String> organs;

    /** 目标剂量 */
    private Double targetDosage;

    /** 选择的回归模型：LINEAR/POLYNOMIAL/LOGARITHMIC */
    private String selectedModel;

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

    public List<String> getOrgans() {
        return organs;
    }

    public void setOrgans(List<String> organs) {
        this.organs = organs;
    }

    public Double getTargetDosage() {
        return targetDosage;
    }

    public void setTargetDosage(Double targetDosage) {
        this.targetDosage = targetDosage;
    }

    public String getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
    }
}
