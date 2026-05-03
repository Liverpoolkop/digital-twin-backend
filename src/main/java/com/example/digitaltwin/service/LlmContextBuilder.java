package com.example.digitaltwin.service;

import com.example.digitaltwin.config.AiPredictionConfig;
import com.example.digitaltwin.entity.DatasetRaw;
import com.example.digitaltwin.mapper.DatasetRawMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM上下文构建器
 * 负责将历史实验数据转换为AI可理解的上下文格式
 */
@Slf4j
@Component
public class LlmContextBuilder {

    private final DatasetRawMapper datasetRawMapper;
    private final AiPredictionConfig config;
    private final ObjectMapper objectMapper;

    public LlmContextBuilder(DatasetRawMapper datasetRawMapper,
                            AiPredictionConfig config,
                            ObjectMapper objectMapper) {
        this.datasetRawMapper = datasetRawMapper;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * 构建AI预测的历史数据上下文
     *
     * @param animalType 物种
     * @param chemicalName 化学物质
     * @param indicatorName 指标名称
     * @return JSON格式的历史数据上下文
     */
    public String buildHistoricalContext(String animalType, String chemicalName, String indicatorName) {
        // 查询相关历史数据（温度范围放宽以获取更多样本）
        List<DatasetRaw> historicalData = datasetRawMapper.selectTrainingData(
            animalType,
            chemicalName,
            indicatorName,
            BigDecimal.valueOf(15.0),  // 最低温度
            BigDecimal.valueOf(30.0)   // 最高温度
        );

        if (historicalData.isEmpty()) {
            return "[]";
        }

        // 限制上下文大小
        List<DatasetRaw> limitedData = historicalData.stream()
            .limit(config.getContextSize())
            .collect(Collectors.toList());

        // 转换为简化的JSON格式
        List<Map<String, Object>> contextData = limitedData.stream()
            .map(data -> {
                Map<String, Object> item = new HashMap<>();
                item.put("dosage", data.getDosage().doubleValue());
                item.put("indicatorValue", data.getIndicatorValue().doubleValue());
                item.put("temperature", data.getTemperature().doubleValue());
                return item;
            })
            .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(contextData);
        } catch (Exception e) {
            log.error("转换历史数据为JSON失败", e);
            return "[]";
        }
    }

    /**
     * 构建System Prompt（定义AI角色和任务）
     */
    public String buildSystemPrompt() {
        return """
            你是一位资深的实验毒理学家，擅长基于历史实验数据预测动物生理指标的时间变化曲线。

            你的专业能力：
            1. 理解剂量-反应关系的非线性特征
            2. 考虑生物学饱和效应、阈值效应和时间依赖性
            3. 基于历史数据推断目标剂量下的指标变化趋势

            任务要求：
            - 预测从给药时刻(0h)到12小时内，每0.5小时的指标变化
            - 必须返回严格的JSON数组格式
            - 格式示例：[{"time":0,"value":85.2},{"time":0.5,"value":87.1},...,{"time":12,"value":95.3}]
            - 共需25个数据点（0, 0.5, 1.0, ..., 11.5, 12.0）
            - 数值需符合生理学合理范围，避免负值或异常突变

            重要：只返回JSON数组，不要添加任何解释文字。
            """;
    }

    /**
     * 构建User Prompt（包含具体实验参数和历史数据）
     */
    public String buildUserPrompt(String animalType, String chemicalName,
                                  Double targetDosage, String indicatorName,
                                  String historicalContext) {
        return String.format("""
            实验参数：
            - 物种：%s
            - 化学物质：%s
            - 目标剂量：%.2f mg/kg
            - 观测指标：%s

            历史实验数据（剂量-反应关系）：
            %s

            请基于以上历史数据，预测在目标剂量 %.2f mg/kg 下，该指标从0h到12h的时间变化曲线。
            直接返回JSON数组，格式：[{"time":0,"value":...},{"time":0.5,"value":...},...,{"time":12,"value":...}]
            """,
            animalType, chemicalName, targetDosage, indicatorName,
            historicalContext, targetDosage);
    }
}
