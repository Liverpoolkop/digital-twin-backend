package com.example.digitaltwin.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 多器官协同仿真结果
 */
public class MultiOrganSimulationResult {

    /** 目标动物 */
    private String targetAnimal;

    /** 目标化学物质 */
    private String targetChemical;

    /** 目标剂量 */
    private Double targetDosage;

    /** 选择的模型 */
    private String selectedModel;

    /** 器官结果映射：key=器官名, value=该器官的所有指标结果 */
    private Map<String, List<OrganIndicatorResult>> organResults;

    /** 仿真时间 */
    private LocalDateTime simulationTime;

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

    public Map<String, List<OrganIndicatorResult>> getOrganResults() {
        return organResults;
    }

    public void setOrganResults(Map<String, List<OrganIndicatorResult>> organResults) {
        this.organResults = organResults;
    }

    public LocalDateTime getSimulationTime() {
        return simulationTime;
    }

    public void setSimulationTime(LocalDateTime simulationTime) {
        this.simulationTime = simulationTime;
    }
}
