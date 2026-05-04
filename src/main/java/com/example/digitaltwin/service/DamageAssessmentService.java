package com.example.digitaltwin.service;

import com.example.digitaltwin.entity.IndicatorBaseline;
import com.example.digitaltwin.mapper.IndicatorBaselineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 器官受损评估服务
 * 根据预测值和基准值计算受损评分（0-100）
 */
@Service
public class DamageAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(DamageAssessmentService.class);

    private final IndicatorBaselineMapper baselineMapper;

    public DamageAssessmentService(IndicatorBaselineMapper baselineMapper) {
        this.baselineMapper = baselineMapper;
    }

    /**
     * 计算指标受损评分
     *
     * @param species 物种
     * @param organName 器官名称
     * @param indicatorName 指标名称
     * @param predictedValue 预测值
     * @return 受损评分 0-100（0=健康，100=极度危险）
     */
    public Double calculateDamageScore(String species, String organName,
                                       String indicatorName, Double predictedValue) {
        // 1. 查询基准值
        IndicatorBaseline baseline = baselineMapper.selectByCondition(species, organName, indicatorName);

        if (baseline == null) {
            log.warn("[受损评估] 未找到基准数据: species={}, organ={}, indicator={}",
                    species, organName, indicatorName);
            return 0.0; // 无基准数据，默认0分
        }

        double normalMin = baseline.getNormalMin().doubleValue();
        double normalMax = baseline.getNormalMax().doubleValue();
        double dangerThreshold = baseline.getDangerThreshold().doubleValue();
        double normalMean = (normalMin + normalMax) / 2.0;

        double score;

        // 2. 根据预测值所在区间计算受损值
        if (predictedValue >= normalMin && predictedValue <= normalMax) {
            // 正常范围内：根据偏离中值的程度给 0-10 分
            double deviation = Math.abs(predictedValue - normalMean);
            double maxDeviation = (normalMax - normalMin) / 2.0;
            score = (deviation / maxDeviation) * 10.0;

        } else if (predictedValue > normalMax) {
            // 高于正常值：线性映射到 10-100
            double excessRatio = (predictedValue - normalMean) / (dangerThreshold - normalMean);
            score = Math.min(excessRatio * 100.0, 100.0);

        } else {
            // 低于正常值：某些指标过低也危险
            double deficitRatio = (normalMean - predictedValue) / normalMean;
            score = Math.min(deficitRatio * 100.0, 100.0);
        }

        log.debug("[受损评估] {}_{}_{}={}, 评分={}", species, organName, indicatorName, predictedValue, score);
        return score;
    }
}
