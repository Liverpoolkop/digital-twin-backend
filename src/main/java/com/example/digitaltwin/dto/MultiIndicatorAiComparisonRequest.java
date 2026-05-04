package com.example.digitaltwin.dto;

import java.util.List;

/**
 * 多指标AI对比请求
 */
public class MultiIndicatorAiComparisonRequest {
    private String animalType;
    private String chemicalName;
    private List<String> indicatorNames;
    private Double targetDosage;
    private Double minTemp = 15.0;
    private Double maxTemp = 30.0;

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

    public List<String> getIndicatorNames() {
        return indicatorNames;
    }

    public void setIndicatorNames(List<String> indicatorNames) {
        this.indicatorNames = indicatorNames;
    }

    public Double getTargetDosage() {
        return targetDosage;
    }

    public void setTargetDosage(Double targetDosage) {
        this.targetDosage = targetDosage;
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
}
