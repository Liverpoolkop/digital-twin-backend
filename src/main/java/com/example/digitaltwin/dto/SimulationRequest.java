package com.example.digitaltwin.dto;

import lombok.Data;

/**
 * 仿真接口请求体
 * POST /api/simulation/run
 */
@Data
public class SimulationRequest {

    /** 操作人 ID（关联 user 表）*/
    private Long userId;

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
}
