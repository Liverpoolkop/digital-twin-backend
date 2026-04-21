package com.example.digitaltwin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实验方案台账（数字孪生模拟用）
 */
@Data
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

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
