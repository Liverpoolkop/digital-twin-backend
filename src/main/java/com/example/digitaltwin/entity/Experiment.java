package com.example.digitaltwin.entity;

import java.time.LocalDateTime;

/**
 * 实验方案/申请单
 */
public class Experiment {

    private Long id;
    /** 实验方案名称 */
    private String name;
    /** 实验动物：MOUSE / RABBIT / FROG */
    private String animalType;
    /** 化学物质名称（与 dataset_raw 训练语料一致） */
    private String chemicalName;
    /** 观测指标名称（与 dataset_raw.indicator_name 一致） */
    private String indicatorName;
    /** 方案说明 */
    private String description;
    /** 状态：DRAFT / PENDING / APPROVED / REJECTED */
    private String status;
    /** 提交人 user.id */
    private Long submittedBy;
    /** 审批人 user.id */
    private Long reviewedBy;
    /** 审批时间 */
    private LocalDateTime reviewedTime;
    /** 审批意见 */
    private String reviewComment;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(Long submittedBy) {
        this.submittedBy = submittedBy;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedTime() {
        return reviewedTime;
    }

    public void setReviewedTime(LocalDateTime reviewedTime) {
        this.reviewedTime = reviewedTime;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
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
